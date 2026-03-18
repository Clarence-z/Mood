export const formatMessage = (content) => {
  if (!content || typeof content !== 'string') return ''
  
  try {
    let formatted = content
    
    // 1. 处理标题 (###, ##, #)
    formatted = formatted
      .replace(/^###\s+(.*)$/gm, '<h3 class="message-heading">$1</h3>')
      .replace(/^##\s+(.*)$/gm, '<h2 class="message-heading">$1</h2>')
      .replace(/^#\s+(.*)$/gm, '<h1 class="message-heading">$1</h1>')
    
    // 2. 处理有序列表 (数字. 开头)
    formatted = processOrderedList(formatted)
    
    // 3. 处理无序列表 (- 或 * 开头)
    formatted = processUnorderedList(formatted)
    
    // 4. 处理粗体和斜体
    formatted = formatted
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
    
    // 5. 处理换行和段落
    formatted = processParagraphs(formatted)
    
    return formatted
  } catch (error) {
    console.error('Markdown 解析错误:', error)
    // 出错时返回安全的原始内容
    return content.replace(/\n/g, '<br>')
  }
}

/**
 * 处理有序列表
 */
const processOrderedList = (text) => {
  // 匹配有序列表项：数字. 开头，可能有多级缩进
  const listItemRegex = /^(\s*)(\d+)\.\s+(.*)$/gm
  
  return text.replace(listItemRegex, (match, indent, number, content) => {
    const indentLevel = indent.length / 2 // 假设每级缩进2个空格
    const indentClass = indentLevel > 0 ? ` list-indent-${indentLevel}` : ''
    
    return `<div class="message-list-item ordered${indentClass}">
              <span class="list-number">${number}.</span>
              <span class="list-content">${content}</span>
            </div>`
  })
}

/**
 * 处理无序列表
 */
const processUnorderedList = (text) => {
  // 匹配无序列表项：- 或 * 开头，可能有多级缩进
  const listItemRegex = /^(\s*)[-*]\s+(.*)$/gm
  
  return text.replace(listItemRegex, (match, indent, content) => {
    const indentLevel = indent.length / 2 // 假设每级缩进2个空格
    const indentClass = indentLevel > 0 ? ` list-indent-${indentLevel}` : ''
    
    return `<div class="message-list-item unordered${indentClass}">
              <span class="list-bullet">•</span>
              <span class="list-content">${content}</span>
            </div>`
  })
}

/**
 * 处理段落和换行
 */
const processParagraphs = (text) => {
  // 将连续的空行转换为段落分隔
  let lines = text.split('\n')
  let result = []
  let currentParagraph = []
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 如果是空行，结束当前段落
    if (!line && currentParagraph.length > 0) {
      if (currentParagraph.join(' ').trim()) {
        result.push(`<p class="message-paragraph">${currentParagraph.join(' ')}</p>`)
      }
      currentParagraph = []
    } 
    // 如果是标题或列表项，直接添加
    else if (line.match(/^<h[1-3]|^<div class="message-list-item/)) {
      // 如果当前段落有内容，先保存
      if (currentParagraph.length > 0) {
        result.push(`<p class="message-paragraph">${currentParagraph.join(' ')}</p>`)
        currentParagraph = []
      }
      result.push(line)
    }
    // 普通文本行，添加到当前段落
    else if (line) {
      currentParagraph.push(line)
    }
  }
  
  // 处理最后一个段落
  if (currentParagraph.length > 0 && currentParagraph.join(' ').trim()) {
    result.push(`<p class="message-paragraph">${currentParagraph.join(' ')}</p>`)
  }
  
  return result.join('')
}

/**
 * 纯文本预览（用于对话列表等地方）
 */
export const getTextPreview = (content, maxLength = 50) => {
  if (!content) return ''
  
  // 移除 Markdown 标记
  let text = content
    .replace(/#{1,3}\s?/g, '') // 标题
    .replace(/\*\*(.*?)\*\*/g, '$1') // 粗体
    .replace(/\*(.*?)\*/g, '$1') // 斜体
    .replace(/^\s*[-*]\s+/gm, '') // 列表项
    .replace(/^\s*\d+\.\s+/gm, '') // 有序列表
  
  // 移除多余的空格和换行
  text = text.replace(/\s+/g, ' ').trim()
  
  // 截断并添加省略号
  if (text.length > maxLength) {
    text = text.substring(0, maxLength) + '...'
  }
  
  return text
}