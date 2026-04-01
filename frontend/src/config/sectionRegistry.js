/**
 * 简历模块注册表 — 唯一数据源
 *
 * 新增模块只需在此数组中添加一个条目：
 *   - key:         模块标识，与后端 ResumeSectionEnum.key 保持一致
 *   - labels:      中英文标题
 *   - contentKey:  对应 resume.content 中的字段名（header/summary 特殊处理，为 null）
 *   - defaultItem: 点击"添加"时插入的空条目结构（字符串字段用 null 表示）
 */
export const SECTION_REGISTRY = [
  {
    key: 'header',
    labels: { zh: '个人信息', en: 'Contact' },
    contentKey: null,
    defaultItem: null,
  },
  {
    key: 'summary',
    labels: { zh: '个人简介', en: 'Professional Summary' },
    contentKey: 'summary',
    defaultItem: null, // 字符串字段，不是列表
  },
  {
    key: 'skills',
    labels: { zh: '专业技能', en: 'Skills' },
    contentKey: 'skills',
    defaultItem: { name: '', level: 80 },
  },
  {
    key: 'experience',
    labels: { zh: '工作经历', en: 'Work Experience' },
    contentKey: 'experience',
    defaultItem: { company: '', position: '', period: '', description: '' },
  },
  {
    key: 'projects',
    labels: { zh: '项目经历', en: 'Projects' },
    contentKey: 'projects',
    defaultItem: { name: '', company: '', title: '', period: '', description: '', technologiesString: '' },
  },
  {
    key: 'works',
    labels: { zh: '个人作品', en: 'Portfolio' },
    contentKey: 'works',
    defaultItem: { name: '', role: '', description: '', url: '' },
  },
  {
    key: 'education',
    labels: { zh: '教育经历', en: 'Education' },
    contentKey: 'education',
    defaultItem: { school: '', major: '', degree: '', period: '' },
  },
  {
    key: 'honors',
    labels: { zh: '荣誉证书', en: 'Awards & Honors' },
    contentKey: 'honors',
    defaultItem: { name: '', date: '', description: '' },
  },
]

/** 默认渲染顺序，由注册表声明顺序自动推导 */
export const DEFAULT_SECTION_ORDER = SECTION_REGISTRY.map(s => s.key)

/** 按 key 快速查找模块元数据 */
export const getSectionMeta = (key) => SECTION_REGISTRY.find(s => s.key === key)

/** 获取指定语言的标签 */
export const getSectionLabel = (key, lang = 'zh') => {
  const meta = getSectionMeta(key)
  return meta?.labels[lang] ?? meta?.labels.zh ?? key
}
