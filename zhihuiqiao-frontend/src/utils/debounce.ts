/**
 * 简易防抖函数
 * @param fn 需要防抖执行的函数
 * @param delay 延迟时间（毫秒）
 * @returns 包装后的防抖函数
 */
export function debounce<T extends (...args: any[]) => void>(fn: T, delay: number = 300): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
    }, delay)
  }
}
