package per.midas.kurumishell.websocket.handler

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import per.midas.kurumishell.websocket.service.SSHService

class SSHWebSocketHandler(
    private val sshService: SSHService
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        try {
            val connectionId = session.uri?.query?.split("=")?.last()
                ?: throw IllegalArgumentException("Missing connectionId parameter")

            sshService.connectToSSH(session, connectionId)
        } catch (e: Exception) {
            session.sendMessage(TextMessage("Error: ${e.message}"))
            session.close()
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        sshService.handleClientMessage(session.id, message.payload)
    }

    // 正确覆盖连接关闭方法
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sshService.cleanupResources(session.id)
    }

    // 处理传输错误
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        sshService.cleanupResources(session.id)
        session.close()
    }
}