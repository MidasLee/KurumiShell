/*
 * @Author: Midas 1010500226@qq.com
 * @Date: 2025-12-10 10:53:05
 * @LastEditors: Midas 1010500226@qq.com
 * @LastEditTime: 2026-01-04 11:03:31
 * @FilePath: /KurumiShell/web/src/utils/env.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
export const getEnv = () => {
  const env = import.meta.env
  return {
    appTitle: env.VITE_APP_TITLE,
    appPort: env.VITE_APP_PORT,
    baseUrl: env.VITE_APP_BASE_URL,
    dudubirdUrl: env.VITE_APP_DUDUBIRD_URL,
    xuanyuanUrl: env.VITE_APP_XUANYUAN_URL
  }
}

export const buildWsUrl = (path: string, connectionId: string): string => {
  const env = import.meta.env
  const baseUrl = env.VITE_APP_BASE_URL;
  let wsBaseUrl = '';

  // 核心判断逻辑：根据VITE_APP_BASE_URL格式自动识别部署模式
  if (baseUrl.startsWith('http')) {
    // 场景1：配置为真实IP+端口（如http://192.168.1.100:8888）
    // 替换http为ws，构建直接连接的WS URL
    wsBaseUrl = baseUrl.replace('http://', 'ws://').replace('https://', 'wss://');
  } else if (baseUrl.startsWith('/')) {
    // 场景2：配置为Nginx代理路径（如/base）
    // 拼接当前域名+代理路径，自动适配ws/wss
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    wsBaseUrl = `${protocol}//${window.location.host}${baseUrl}`;
  } else {
    // 兜底：默认使用当前域名+根路径
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    wsBaseUrl = `${protocol}//${window.location.host}`;
    console.warn('VITE_APP_BASE_URL配置格式不规范，已使用默认路径', '配置值:', baseUrl);
  }

  // 拼接完整的WebSocket接口路径
  return `${wsBaseUrl}/api/ssh/${path}?connectionId=${connectionId}`;
};