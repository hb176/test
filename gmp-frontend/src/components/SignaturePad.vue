<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = withDefaults(defineProps<{ width?: number; height?: number }>(), { width: 400, height: 200 })
const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let drawing = false
let hasSignature = false

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = props.width
  canvas.height = props.height
  ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.strokeStyle = '#333'
  drawPlaceholder()
}

function drawPlaceholder() {
  if (!ctx || hasSignature) return
  ctx.clearRect(0, 0, props.width, props.height)
  ctx.save()
  ctx.strokeStyle = '#ccc'
  ctx.setLineDash([5, 5])
  ctx.strokeRect(2, 2, props.width - 4, props.height - 4)
  ctx.fillStyle = '#ccc'
  ctx.font = '14px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('请在此处签名', props.width / 2, props.height / 2)
  ctx.restore()
}

function getPos(e: MouseEvent | TouchEvent) {
  const rect = canvasRef.value!.getBoundingClientRect()
  const touch = 'touches' in e ? e.touches[0] : e
  return { x: touch.clientX - rect.left, y: touch.clientY - rect.top }
}

function onStart(e: MouseEvent | TouchEvent) {
  if (!ctx) return
  drawing = true
  if (!hasSignature) {
    ctx.clearRect(0, 0, props.width, props.height)
    ctx.strokeStyle = '#333'
    ctx.setLineDash([])
    hasSignature = true
  }
  const { x, y } = getPos(e)
  ctx.beginPath()
  ctx.moveTo(x, y)
}

function onMove(e: MouseEvent | TouchEvent) {
  if (!drawing || !ctx) return
  e.preventDefault()
  const { x, y } = getPos(e)
  ctx.lineTo(x, y)
  ctx.stroke()
}

function onEnd() {
  drawing = false
}

function clear() {
  hasSignature = false
  drawPlaceholder()
}

function isEmpty(): boolean {
  return !hasSignature
}

function toDataURL(): string {
  return canvasRef.value?.toDataURL('image/png') || ''
}

defineExpose({ clear, isEmpty, toDataURL })

onMounted(initCanvas)
onBeforeUnmount(() => { ctx = null })
</script>

<template>
  <canvas
    ref="canvasRef"
    :style="{ border: '1px solid #dcdfe6', borderRadius: '4px', cursor: 'crosshair', touchAction: 'none' }"
    @mousedown="onStart"
    @mousemove="onMove"
    @mouseup="onEnd"
    @mouseleave="onEnd"
    @touchstart.prevent="onStart"
    @touchmove.prevent="onMove"
    @touchend="onEnd"
  />
</template>
