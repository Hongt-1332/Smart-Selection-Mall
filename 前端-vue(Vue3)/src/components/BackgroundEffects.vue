<template>
  <div class="bg-effects">
    <!-- 星空背景 -->
    <div class="stars-container">
      <div v-for="i in 60" :key="'star-'+i" class="star" :style="getStarStyle(i)"></div>
    </div>

    <!-- 十二星座 SVG -->
    <div class="constellations">
      <div
        v-for="(cz, idx) in zodiacs"
        :key="cz.name"
        class="constellation"
        :style="cz.style"
      >
        <svg :width="cz.size" :height="cz.size" viewBox="0 0 100 100" class="cz-svg">
          <!-- 连线 -->
          <polyline
            v-if="cz.lines"
            :points="cz.lines"
            fill="none"
            stroke="rgba(255,255,255,0.45)"
            stroke-width="1.2"
            stroke-dasharray="4,4"
          />
          <!-- 星星 -->
          <circle
            v-for="(pt, pi) in cz.points"
            :key="pi"
            :cx="pt.x"
            :cy="pt.y"
            r="2.5"
            fill="#fff"
            :class="'star-glow star-glow-' + (pi % 3)"
            :style="{ animationDelay: (idx * 0.4 + pi * 0.3) + 's' }"
          />
          <!-- 星座名称 -->
          <text
            :x="cz.labelX || 50"
            :y="cz.labelY || 95"
            text-anchor="middle"
            fill="rgba(255,255,255,0.55)"
            font-size="8"
            font-family="serif"
          >{{ cz.symbol }} {{ cz.name }}</text>
        </svg>
      </div>
    </div>

    <!-- 流星 -->
    <div v-for="i in 80" :key="'meteor-'+i" class="meteor" :style="getMeteorStyle(i)"></div>
  </div>
</template>

<script setup lang="ts">
interface Point { x: number; y: number }
interface Zodiac {
  name: string
  symbol: string
  points: Point[]
  lines?: string
  size: number
  labelX?: number
  labelY?: number
  style: Record<string, string>
}

const zodiacs: Zodiac[] = [
  {
    name: '白羊座', symbol: '♈',
    points: [{x:50,y:15},{x:65,y:35},{x:55,y:50},{x:40,y:40},{x:30,y:55},{x:45,y:70},{x:60,y:65}],
    lines: '50,15 65,35 55,50 40,40 30,55 45,70 60,65',
    size: 180, labelY: 110,
    style: { top: '2%', left: '2%' }
  },
  {
    name: '金牛座', symbol: '♉',
    points: [{x:30,y:20},{x:50,y:10},{x:70,y:20},{x:65,y:40},{x:45,y:45},{x:25,y:40},{x:35,y:60},{x:55,y:65}],
    lines: '30,20 50,10 70,20 65,40 45,45 25,40 35,60 55,65',
    size: 180,
    style: { top: '2%', right: '5%' }
  },
  {
    name: '双子座', symbol: '♊',
    points: [{x:35,y:15},{x:50,y:5},{x:65,y:15},{x:55,y:35},{x:40,y:30},{x:45,y:55},{x:60,y:50},{x:55,y:75}],
    lines: '35,15 50,5 65,15 55,35 40,30 45,55 60,50 55,75',
    size: 180,
    style: { top: '16%', left: '6%' }
  },
  {
    name: '巨蟹座', symbol: '♋',
    points: [{x:40,y:10},{x:60,y:10},{x:70,y:30},{x:55,y:45},{x:40,y:40},{x:25,y:55},{x:40,y:70},{x:60,y:65}],
    lines: '40,10 60,10 70,30 55,45 40,40 25,55 40,70 60,65',
    size: 180,
    style: { top: '14%', right: '2%' }
  },
  {
    name: '狮子座', symbol: '♌',
    points: [{x:50,y:5},{x:65,y:20},{x:55,y:40},{x:35,y:30},{x:25,y:45},{x:40,y:60},{x:60,y:55},{x:65,y:75}],
    lines: '50,5 65,20 55,40 35,30 25,45 40,60 60,55 65,75',
    size: 180,
    style: { top: '30%', left: '1%' }
  },
  {
    name: '处女座', symbol: '♍',
    points: [{x:45,y:5},{x:60,y:15},{x:70,y:35},{x:55,y:50},{x:35,y:45},{x:25,y:60},{x:40,y:75},{x:60,y:70},{x:50,y:90}],
    lines: '45,5 60,15 70,35 55,50 35,45 25,60 40,75 60,70 50,90',
    size: 180,
    style: { top: '28%', right: '8%' }
  },
  {
    name: '天秤座', symbol: '♎',
    points: [{x:30,y:15},{x:50,y:5},{x:70,y:15},{x:65,y:35},{x:35,y:35},{x:25,y:55},{x:45,y:65},{x:65,y:55},{x:55,y:80}],
    lines: '30,15 50,5 70,15 65,35 35,35 25,55 45,65 65,55 55,80',
    size: 180,
    style: { top: '44%', left: '5%' }
  },
  {
    name: '天蝎座', symbol: '♏',
    points: [{x:50,y:5},{x:65,y:20},{x:55,y:40},{x:35,y:30},{x:25,y:50},{x:45,y:60},{x:65,y:55},{x:75,y:75},{x:55,y:85}],
    lines: '50,5 65,20 55,40 35,30 25,50 45,60 65,55 75,75 55,85',
    size: 180,
    style: { top: '42%', right: '3%' }
  },
  {
    name: '射手座', symbol: '♐',
    points: [{x:40,y:5},{x:60,y:10},{x:70,y:30},{x:55,y:45},{x:35,y:40},{x:20,y:55},{x:40,y:70},{x:60,y:60},{x:50,y:85}],
    lines: '40,5 60,10 70,30 55,45 35,40 20,55 40,70 60,60 50,85',
    size: 180,
    style: { top: '58%', left: '2%' }
  },
  {
    name: '摩羯座', symbol: '♑',
    points: [{x:35,y:5},{x:55,y:5},{x:65,y:25},{x:45,y:35},{x:25,y:25},{x:15,y:45},{x:35,y:60},{x:55,y:55},{x:65,y:75}],
    lines: '35,5 55,5 65,25 45,35 25,25 15,45 35,60 55,55 65,75',
    size: 180,
    style: { top: '56%', right: '10%' }
  },
  {
    name: '水瓶座', symbol: '♒',
    points: [{x:45,y:5},{x:60,y:15},{x:75,y:5},{x:70,y:30},{x:50,y:35},{x:30,y:25},{x:20,y:45},{x:40,y:55},{x:60,y:50},{x:50,y:75}],
    lines: '45,5 60,15 75,5 70,30 50,35 30,25 20,45 40,55 60,50 50,75',
    size: 180,
    style: { top: '72%', left: '8%' }
  },
  {
    name: '双鱼座', symbol: '♓',
    points: [{x:50,y:5},{x:65,y:20},{x:55,y:40},{x:35,y:30},{x:20,y:45},{x:35,y:60},{x:55,y:55},{x:65,y:70},{x:50,y:85},{x:40,y:70}],
    lines: '50,5 65,20 55,40 35,30 20,45 35,60 55,55 65,70 50,85 40,70',
    size: 180,
    style: { top: '70%', right: '2%' }
  },
]

function getStarStyle(i: number) {
  const x = (i * 17 + 31) % 100
  const y = (i * 13 + 7) % 100
  const size = 2 + (i % 4) * 0.5
  const delay = (i * 0.7) % 4
  const duration = 2 + (i % 3)
  return {
    left: x + '%',
    top: y + '%',
    width: size + 'px',
    height: size + 'px',
    animationDelay: delay + 's',
    animationDuration: duration + 's',
  }
}

function getMeteorStyle(i: number) {
  const seed = i * 17 + 3
  // 角度统一左上→右下：-30° ~ -55°
  const angle = -30 - (seed % 26)
  // 起点分布（左上方）
  const top = (seed % 60) - 5
  const left = (seed * 7 + 11) % 80 - 5
  // 延迟：0 ~ 40s
  const delay = (seed % 40) + (i % 7) * 0.5
  // 持续：3 ~ 7s
  const duration = 3 + (seed % 40) * 0.1
  // 大小：6 ~ 22px
  const size = 6 + (seed % 17)
  // 颜色方案
  const colorSchemes = [
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
  const color = colorSchemes[seed % colorSchemes.length]
  const glow = size * 2 + (seed % 4) * size * 0.3

  return {
    top: top + '%',
    left: left + '%',
    width: size + 'px',
    height: size + 'px',
    animationDelay: delay + 's',
    animationDuration: duration + 's',
    '--angle': angle + 'deg',
    '--color': color,
    '--glow': glow + 'px',
  }
}
</script>

<style scoped>
.bg-effects {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* ---- 星星 ---- */
.stars-container {
  position: absolute;
  inset: 0;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: twinkle ease-in-out infinite alternate;
  box-shadow: 0 0 5px 2px rgba(255,255,255,0.7);
  opacity: 0.75;
}

@keyframes twinkle {
  0%   { opacity: 0.35; transform: scale(0.8); }
  50%  { opacity: 1; transform: scale(1.3); }
  100% { opacity: 0.5; transform: scale(0.9); }
}

/* ---- 星座 ---- */
.constellations {
  position: absolute;
  inset: 0;
}

.constellation {
  position: absolute;
  opacity: 0.65;
  transition: opacity 0.8s;
}

.constellation:hover {
  opacity: 0.9;
}

.cz-svg {
  width: 100%;
  height: 100%;
}

.star-glow {
  filter: drop-shadow(0 0 8px rgba(255,255,255,1));
  animation: pulse-star 2s ease-in-out infinite alternate;
}

.star-glow-0 { animation-duration: 2.0s; }
.star-glow-1 { animation-duration: 2.8s; }
.star-glow-2 { animation-duration: 3.5s; }

@keyframes pulse-star {
  0%   { opacity: 0.6; }
  100% { opacity: 1; }
}

/* ---- 流星 (光球) ---- */
.meteor {
  position: absolute;
  border-radius: 50%;
  opacity: 0;
  animation: meteor-fly linear infinite;
  pointer-events: none;
  will-change: transform, opacity;
  background: radial-gradient(circle at 50% 50%,
    transparent 20%,
    var(--color, rgba(255,255,255,0.85)) 50%,
    transparent 100%
  );
  box-shadow:
    0 0 calc(var(--glow, 20px) * 0.8) calc(var(--glow, 20px) * 0.4) var(--color, rgba(255,255,255,0.6)),
    0 0 var(--glow, 20px) calc(var(--glow, 20px) * 0.8) var(--color, rgba(255,255,255,0.4));
  filter: blur(1.2px);
}

@keyframes meteor-fly {
  0%   {
    opacity: 0;
    transform: translate(0, 0) rotate(var(--angle, -40deg)) scale(0.15);
  }
  2%   {
    opacity: 0.85;
  }
  10%  {
    opacity: 0.95;
    transform: translate(200px, 280px) rotate(var(--angle, -40deg)) scale(1);
  }
  25%  {
    opacity: 0.8;
    transform: translate(450px, 630px) rotate(var(--angle, -40deg)) scale(0.85);
  }
  45%  {
    opacity: 0.55;
    transform: translate(750px, 1050px) rotate(var(--angle, -40deg)) scale(0.6);
  }
  65%  {
    opacity: 0.25;
    transform: translate(1050px, 1470px) rotate(var(--angle, -40deg)) scale(0.35);
  }
  85%  {
    opacity: 0.08;
    transform: translate(1350px, 1890px) rotate(var(--angle, -40deg)) scale(0.15);
  }
  100% {
    opacity: 0;
    transform: translate(1600px, 2240px) rotate(var(--angle, -40deg)) scale(0);
  }
}
</style>