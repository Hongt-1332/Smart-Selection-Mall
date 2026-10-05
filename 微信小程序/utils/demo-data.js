/**
 * 内置演示数据
 *
 * 用途：接口不可用、无缓存时兜底渲染，保证页面信息完整可交互。
 * 约定：演示商品的 id 使用负数（-1、-2…），与后端真实 id 不会冲突，
 *       便于详情页识别并走本地渲染分支。
 *
 * ⚠️ 图片策略：只使用「项目内本地图片」，绝不引用外部域名。
 * 小程序真机对未在后台配置的域名会直接拦截（如 picsum.photos），
 * 而本地资源走包内路径，开发版 / 体验版 / 正式版都稳定可用。
 */
const { money } = require('./format')

/** 兜底图片：项目内本地资源（合法域名白名单无关） */
const FALLBACK_IMAGE = '/image/1.png'

/** 首页 Banner 兜底图 */
const DEFAULT_BANNERS = [FALLBACK_IMAGE]

const DEMO_GOODS_SEED = [
  { name: '精选好物', price: 99, desc: '品质优选 · 官方直营' },
  { name: '热销爆款', price: 199, desc: '人气之选 · 限时折扣' },
  { name: '新品推荐', price: 299, desc: '当季新品 · 抢先体验' },
  { name: '限时特惠', price: 399, desc: '超值特惠 · 数量有限' },
  { name: '品质优选', price: 499, desc: '精选好货 · 值得信赖' },
  { name: '潮流入门', price: 129, desc: '入门必选 · 轻巧便携' },
  { name: '旗舰臻品', price: 899, desc: '旗舰配置 · 极致体验' },
  { name: '日常刚需', price: 59, desc: '日用常备 · 性价比高' },
]

/**
 * 生成内置演示商品列表
 * @returns {Array} 商品数组（id 为负数，图片使用本地资源）
 */
function buildDemoGoods() {
  return DEMO_GOODS_SEED.map((s, i) => ({
    id: -(i + 1),
    goodsName: s.name,
    describe: s.desc,
    goodsPrice: s.price,
    goodsPriceText: money(s.price),
    goodsStock: 100,
    // 本地兜底图，保证兜底数据也有图可看
    imagePath: FALLBACK_IMAGE,
    userName: '官方店铺',
    merchantId: 0,
    userId: 0,
    address: '北京市朝阳区科技园区',
    cartQuantity: 0,
    /** 标记为内置演示数据 */
    isDemo: true,
  }))
}

/** 是否为演示商品（id 为负数） */
function isDemoGoodsId(id) {
  const n = Number(id)
  return !isNaN(n) && n < 0
}

/** 按 id 查找演示商品 */
function findDemoGoods(id) {
  return buildDemoGoods().find((g) => String(g.id) === String(id)) || null
}

module.exports = {
  FALLBACK_IMAGE,
  DEFAULT_BANNERS,
  buildDemoGoods,
  isDemoGoodsId,
  findDemoGoods,
}
