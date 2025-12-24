package per.midas.kurumishell.service

import com.jcraft.jsch.*
import org.springframework.stereotype.Service
import per.midas.kurumishell.entity.SSHConnection
import per.midas.kurumishell.entity.User
import per.midas.kurumishell.dto.request.SSHFileOperationRequest
import per.midas.kurumishell.dto.response.SSHFileInfoResponse
import per.midas.kurumishell.repository.SSHConnectionRepository
import java.io.*

@Service
class SSHFileManagementService(
    private val connectionRepository: SSHConnectionRepository,
    private val sshConnectionPool: SSHConnectionPool
) {
    
    /**
     * 获取文件列表
     */
    fun getFileList(connectionId: String, user: User, path: String): List<Map<String, Any>> {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                val fileList = mutableListOf<Map<String, Any>>()
                val entries = channel.ls(path)
                
                for (entry in entries) {
                    val sftpATTRS = entry.attrs
                    if (entry.filename != "." && entry.filename != "..") {
                        val fileInfo = mapOf<String, Any>(
                            "name" to entry.filename,
                            "path" to "$path/${entry.filename}",
                            "size" to sftpATTRS.size,
                            "isDirectory" to sftpATTRS.isDir,
                            "permissions" to getPermissionsString(sftpATTRS.permissions),
                            "owner" to "-",
                            "group" to "-",
                            "modifiedTime" to System.currentTimeMillis(),
                            "canRead" to ((sftpATTRS.permissions and 4) != 0),
                            "canWrite" to ((sftpATTRS.permissions and 2) != 0),
                            "canExecute" to ((sftpATTRS.permissions and 1) != 0)
                        )
                        val sshFileInfo = SSHFileInfoResponse.fromFileInfo(fileInfo)
                        fileList.add(sshFileInfo.toFrontendFormat())
                    }
                }
                fileList
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 读取文件内容
     */
    fun readFile(connectionId: String, user: User, request: SSHFileOperationRequest): String {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                val inputStream = channel.get(path)
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 写入文件内容
     */
    fun writeFile(connectionId: String, user: User, request: SSHFileOperationRequest): Boolean {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                val tempFile = File.createTempFile("ssh_upload_", ".tmp")
                try {
                    FileWriter(tempFile).use { writer ->
                        writer.write(request.content ?: "")
                    }
                    channel.put(tempFile.absolutePath, path)
                    true
                } finally {
                    tempFile.delete()
                }
            } catch (e: SftpException) {
                throw IllegalArgumentException("写入文件失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 创建目录
     */
    fun createDirectory(connectionId: String, user: User, request: SSHFileOperationRequest): Boolean {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("目录路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                channel.mkdir(path)
                true
            } catch (e: SftpException) {
                throw IllegalArgumentException("创建目录失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 删除文件或目录
     */
    fun deleteFile(connectionId: String, user: User, request: SSHFileOperationRequest): Boolean {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("文件或目录路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                val sftpATTRS = channel.stat(path)
                if (sftpATTRS.isDir) {
                    // 如果是目录，递归删除所有内容
                    deleteDirectoryRecursively(channel, path)
                } else {
                    // 如果是文件，直接删除
                    channel.rm(path)
                }
                true
            } catch (e: SftpException) {
                throw IllegalArgumentException("删除文件失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 递归删除目录
     */
    private fun deleteDirectoryRecursively(channel: ChannelSftp, dirPath: String) {
        // 获取目录内容
        val entries = channel.ls(dirPath)
        
        for (entry in entries) {
            val filename = entry.filename
            if (filename != "." && filename != "..") {
                val fullPath = if (dirPath == "/") "/$filename" else "$dirPath/$filename"
                
                try {
                    val attrs = channel.stat(fullPath)
                    if (attrs.isDir) {
                        // 递归删除子目录
                        deleteDirectoryRecursively(channel, fullPath)
                    } else {
                        // 删除文件
                        channel.rm(fullPath)
                    }
                } catch (e: SftpException) {
                    // 如果文件或目录不存在，继续处理其他项
                    continue
                }
            }
        }
        
        // 删除空目录
        channel.rmdir(dirPath)
    }
    
    /**
     * 重命名文件或目录
     */
    fun renameFile(connectionId: String, user: User, request: SSHFileOperationRequest): Boolean {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val sourcePath = request.sourcePath?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("源路径不能为空")
        val targetPath = request.targetPath?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("目标路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                channel.rename(sourcePath, targetPath)
                true
            } catch (e: SftpException) {
                throw IllegalArgumentException("重命名文件失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 上传文件
     */
    fun uploadFile(connectionId: String, user: User, request: SSHFileOperationRequest, fileContent: ByteArray): Boolean {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                // 提取远程路径的目录部分
                val remoteDir = path.substringBeforeLast("/", "/")
                
                // 创建必要的目录结构
                createDirectoriesRecursively(channel, remoteDir)
                
                // 上传文件
                val tempFile = File.createTempFile("ssh_upload_", ".tmp")
                try {
                    FileOutputStream(tempFile).use { output ->
                        output.write(fileContent)
                    }
                    channel.put(tempFile.absolutePath, path)
                    true
                } finally {
                    tempFile.delete()
                }
            } catch (e: SftpException) {
                throw IllegalArgumentException("上传文件失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 递归创建目录结构
     */
    private fun createDirectoriesRecursively(channel: ChannelSftp, dirPath: String) {
        if (dirPath == "/" || dirPath.isEmpty()) {
            return
        }
        
        try {
            // 检查目录是否存在
            channel.stat(dirPath)
        } catch (e: SftpException) {
            // 目录不存在，创建父目录
            val parentDir = dirPath.substringBeforeLast("/", "/")
            createDirectoriesRecursively(channel, parentDir)
            
            // 创建当前目录
            channel.mkdir(dirPath)
        }
    }
    
    /**
     * 下载文件
     */
    fun downloadFile(connectionId: String, user: User, request: SSHFileOperationRequest): ByteArray {
        val connection = connectionRepository.findByIdAndUser(connectionId, user)
            ?: throw IllegalArgumentException("SSH连接不存在或无权访问")
        
        val path = request.path ?: throw IllegalArgumentException("文件路径不能为空")
        
        return sshConnectionPool.executeWithSession(connection) { session ->
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            
            try {
                val tempFile = File.createTempFile("ssh_download_", ".tmp")
                try {
                    channel.get(path, tempFile.absolutePath)
                    FileInputStream(tempFile).use { input ->
                        input.readBytes()
                    }
                } finally {
                    tempFile.delete()
                }
            } catch (e: SftpException) {
                throw IllegalArgumentException("下载文件失败: ${e.message}")
            } finally {
                channel.disconnect()
            }
        }
    }
    
    /**
     * 获取权限字符串
     */
    private fun getPermissionsString(permissions: Int): String {
        return String.format("%03o", permissions and 0xFFF)
    }
}