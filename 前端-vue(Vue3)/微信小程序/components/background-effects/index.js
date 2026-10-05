// components/background-effects/index.js
// 星空 + 十二星座 + 流星 背景特效组件（迁移自 src/components/BackgroundEffects.vue）
//
// 注意：小程序不支持 SVG 标签，星座连线/星点改用 <view> + CSS 定位实现等价效果。

/** 十二星座点位数据（与原项目保持一致） */
const ZODIACS = [
  {
    name: '白羊座', symbol: '♈',
    points: [[50,15],[65,35],[55,50],[40,40],[30,55],[45,70],[60,65]],
    top: '2%', left: '2%', right: '',
  },
  {
    name: '金牛座', symbol: '♉',
    points: [[30,20],[50,10],[70,20],[65,40],[45,45],[25,40],[35,60],[55,65]],
    top: '2%', left: '', right: '5%',
  },
  {
    name: '双子座', symbol: '♊',
    points: [[35,15],[50,5],[65,15],[55,35],[40,30],[45,55],[60,50],[55,75]],
    top: '16%', left: '6%', right: '',
  },
  {
    name: '巨蟹座', symbol: '♋',
    points: [[40,10],[60,10],[70,30],[55,45],[40,40],[25,55],[40,70],[60,65]],
    top: '14%', left: '', right: '2%',
  },
  {
    name: '狮子座', symbol: '♌',
    points: [[50,5],[65,20],[55,40],[35,30],[25,45],[40,60],[60,55],[65,75]],
    top: '30%', left: '1%', right: '',
  },
  {
    name: '处女座', symbol: '♍',
    points: [[45,5],[60,15],[70,35],[55,50],[35,45],[25,60],[40,75],[60,70],[50,90]],
    top: '28%', left: '', right: '8%',
  },
  {
    name: '天秤座', symbol: '♎',
    points: [[30,15],[50,5],[70,15],[65,35],[35,35],[25,55],[45,65],[65,55],[55,80]],
    top: '44%', left: '5%', right: '',
  },
  {
    name: '天蝎座', symbol: '♏',
    points: [[50,5],[65,20],[55,40],[35,30],[25,50],[45,60],[65,55],[75,75],[55,85]],
    top: '42%', left: '', right: '3%',
  },
  {
    name: '射手座', symbol: '♐',
    points: [[40,5],[60,10],[70,30],[55,45],[35,40],[20,55],[40,70],[60,60],[50,85]],
    top: '58%', left: '2%', right: '',
  },
  {
    name: '摩羯座', symbol: '♑',
    points: [[35,5],[55,5],[65,25],[45,35],[25,25],[15,45],[35,60],[55,55],[65,75]],
    top: '56%', left: '', right: '10%',
  },
  {
    name: '水瓶座', symbol: '♒',
    points: [[45,5],[60,15],[75,5],[70,30],[50,35],[30,25],[20,45],[40,55],[60,50],[50,75]],
    top: '72%', left: '8%', right: '',
  },
  {
    name: '双鱼座', symbol: '♓',
    points: [[50,5],[65,20],[55,40],[35,30],[20,45],[35,60],[55,55],[65,70],[50,85],[40,70]],
    top: '70%', left: '', right: '2%',
  },
]

/** 生成星星样式（与原 getStarStyle 一致） */
function buildStars(count) {
  const stars = []
  for (let i = 1; i <= count; i++) {
    const x = (i * 17 + 31) % 100
    const y = (i * 13 + 7) % 100
    const size = 2 + (i % 4) * 0.5
    const delay = ((i * 0.7) % 4).toFixed(1)
    const duration = 2 + (i % 3)
    const half = (size / 2).toFixed(2)
    stars.push({
      key: 'star-' + i,
      style: `left:${x}%;top:${y}%;width:${size}px;height:${size}px;` +
        `margin-left:-${half}px;margin-top:-${half}px;` +
        `animation-delay:${delay}s;animation-duration:${duration}s;`,
    })
  }
  return stars
}

/** 生成流星样式（与原 getMeteorStyle 一致） */
const METEOR_COLORS = [
  'rgba(255,255,255,0.9)',
  'rgba(200,220,255,0.85)',
  'rgba(255,220,255,0.8)',
  'rgba(180,210,255,0.85)',
  'rgba(255,240,220,0.8)',
  'rgba(160,200,255,0.75)',
  'rgba(255,200,240,0.8)',
  'rgba(140,230,255,0.75)',
  'rgba(255,200,180,0.8)',
  'rgba(220,180,255,0.8)',
  'rgba(180,255,220,0.7)',
  'rgba(255,255,200,0.8)',
]

function buildMeteors(count) {
  const meteors = []
  for (let i = 1; i <= count; i++) {
    const seed = i * 17 + 3
    const angle = -30 - (seed % 26)
    const top = (seed % 60) - 5
    const left = ((seed * 7 + 11) % 80) - 5
    const delay = (seed % 40) + (i % 7) * 0.5
    const duration = (3 + (seed % 40) * 0.1).toFixed(1)
    const size = 6 + (seed % 17)
    const color = METEOR_COLORS[seed % METEOR_COLORS.length]
    const glow = (size * 2 + (seed % 4) * size * 0.3).toFixed(1)
    meteors.push({
      key: 'meteor-' + i,
      style: `top:${top}%;left:${left}%;width:${size}px;height:${size}px;` +
        `animation-delay:${delay}s;animation-duration:${duration}s;` +
        `--angle:${angle}deg;--color:${color};--glow:${glow}px;`,
    })
  }
  return meteors
}

/** 生成星座点位（百分比定位 + 连线长度/角度） */
function buildConstellations() {
  return ZODIACS.map((cz, idx) => {
    const pts = cz.points.map((p, pi) => ({
      key: `p${pi}`,
      style: `left:${p[0]}%;top:${p[1]}%;` +
        `animation-delay:${(idx * 0.4 + pi * 0.3).toFixed(1)}s;` +
        `animation-duration:${(2 + (pi % 3) * 0.8).toFixed(1)}s;`,
    }))

    // 相邻点之间画连线：用 rotate + width 模拟
    const lines = []
    for (let k = 0; k < cz.points.length - 1; k++) {
      const a = cz.points[k]
      const b = cz.points[k + 1]
      const dx = b[0] - a[0]
      const dy = b[1] - a[1]
      const len = Math.sqrt(dx * dx + dy * dy)
      const deg = (Math.atan2(dy, dx) * 180) / Math.PI
      lines.push({
        key: `l${k}`,
        style: `left:${a[0]}%;top:${a[1]}%;width:${len.toFixed(2)}%;` +
          `transform:rotate(${deg.toFixed(2)}deg);`,
      })
    }

    return {
      key: cz.name,
      name: cz.name,
      symbol: cz.symbol,
      points: pts,
      lines,
      posStyle:
        `top:${cz.top};` +
        (cz.left ? `left:${cz.left};` : `right:${cz.right};`),
    }
  })
}

Component({
  // 样式隔离已在 index.json 中通过 "styleIsolation": "isolated" 声明
  // （官方文档：自定义组件 JSON 中的 styleIsolation 从基础库 2.10.1 起支持）

  properties: {
    /** 是否显示（默认显示） */
    visible: {
      type: Boolean,
      value: true,
    },
    /** 星星数量，移动端默认比 H5 少一些，保证性能 */
    starCount: {
      type: Number,
      value: 40,
    },
    /** 流星数量，移动端默认减少 */
    meteorCount: {
      type: Number,
      value: 28,
    },
    /** 是否显示十二星座 */
    showZodiac: {
      type: Boolean,
      value: true,
    },
  },

  data: {
    stars: [],
    meteors: [],
    constellations: [],
  },

  lifetimes: {
    attached() {
      this.rebuild()
    },
  },

  observers: {
    'starCount, meteorCount, showZodiac': function () {
      this.rebuild()
    },
  },

  methods: {
    rebuild() {
      this.setData({
        stars: buildStars(this.data.starCount),
        meteors: buildMeteors(this.data.meteorCount),
        constellations: this.data.showZodiac ? buildConstellations() : [],
      })
    },
  },
})
