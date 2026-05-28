<script setup lang="ts">
/**
 * ProcessDesigner — 自研 BPMN 流程设计器组件
 *
 * 基于 bpmn-js@17 + bpmn-js-properties-panel@5 + Flowable moddle 扩展
 * 替代原有的 iframe 方案，提供完整的流程编辑能力
 */
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import MinimapModule from 'diagram-js-minimap'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import { is } from 'bpmn-js/lib/util/ModelUtil'
import flowableDescriptor from './moddle/flowable.json'
import FlowablePropertiesProvider from './provider/FlowablePropertiesProvider'
import customTranslate from './i18n/zh'
import { getBpmnByModelId, saveBpmnModel } from '@/api/bpmn'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  RefreshLeft as ElIconUndo,
  RefreshRight as ElIconRedo,
  ZoomIn as ElIconZoomIn,
  ZoomOut as ElIconZoomOut,
  Download as ElIconDownload,
  MapLocation as ElIconMap,
  Upload as ElIconUpload,
  View as ElIconView,
  Delete as ElIconDelete
} from '@element-plus/icons-vue'

// 自定义组件
import UserTagInput from './components/UserTagInput.vue'
import RoleTagInput from './components/RoleTagInput.vue'
import FormTagInput from './components/FormTagInput.vue'
import ListenerManager from './components/ListenerManager.vue'
import PropertyGroup from './components/PropertyGroup.vue'
import TimerConfig from './components/TimerConfig.vue'
import MultiInstanceConfig from './components/MultiInstanceConfig.vue'
import FormFieldPermission from './components/FormFieldPermission.vue'

// bpmn-js 样式
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import '@bpmn-io/properties-panel/assets/properties-panel.css'
import 'diagram-js-minimap/assets/diagram-js-minimap.css'

// ==================== 中文翻译模块 ====================

const translateModule = {
  translate: ['value', customTranslate]
}

// ==================== Props & Emits ====================

const props = defineProps<{
  modelId?: string
  modelKey?: string
  xml?: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  save: [xml: string, modelId?: string]
  deploy: [modelId: string]
}>()

// ==================== 状态 ====================

const canvasRef = ref<HTMLDivElement>()
const loading = ref(false)
const saving = ref(false)
const zoom = ref(100)
const minimapOpen = ref(false)
const previewVisible = ref(false)
const previewXml = ref('')

// 选中元素状态
const selectedElement = ref<any>(null)
const selectedType = ref('')

// 监听器数据
const executionListeners = ref<any[]>([])
const taskListeners = ref<any[]>([])

let modeler: any = null

// ==================== 计算属性 ====================

const isUserTask = computed(() => selectedType.value === 'bpmn:UserTask')
const isServiceTask = computed(() => selectedType.value === 'bpmn:ServiceTask')
const isScriptTask = computed(() => selectedType.value === 'bpmn:ScriptTask')
const isProcess = computed(() => selectedType.value === 'bpmn:Process')
const isStartEvent = computed(() => selectedType.value === 'bpmn:StartEvent')
const isEndEvent = computed(() => selectedType.value === 'bpmn:EndEvent')
const isGateway = computed(() => selectedType.value?.includes('Gateway'))
const isTimerEvent = computed(() => {
  if (!selectedElement.value) return false
  const bo = selectedElement.value.businessObject || selectedElement.value
  if (!bo || typeof bo.get !== 'function') return false
  const events = bo.get('eventDefinitions')
  return events?.some((e: any) => e.$instanceOf('bpmn:TimerEventDefinition'))
})
const isCallActivity = computed(() => selectedType.value === 'bpmn:CallActivity')
const isSubProcess = computed(() => selectedType.value === 'bpmn:SubProcess')

const showFlowablePanel = computed(() => !!selectedType.value)

// 分组展开状态
const groupOpen = ref<Record<string, boolean>>({
  basic: true,
  assignee: true,
  form: true,
  service: true,
  script: true,
  multiInstance: false,
  async: false,
  timer: true,
  gateway: true,
  callActivity: true,
  taskListener: true,
  executionListener: true,
  extensionProps: false
})

// 流程Key（从根元素获取）
const processKey = computed(() => {
  if (!modeler) return ''
  const root = modeler.get('canvas').getRootElement()
  return root?.businessObject?.id || ''
})

// 菜单绑定
const menuList = ref<any[]>([])
const processMenuKey = ref('')

async function loadMenuList() {
  try {
    const res = await request.get('/system/menu/tree')
    if (res.data) {
      // 只取菜单类型（menuType=1），过滤按钮
      menuList.value = flattenMenus(res.data).filter((m: any) => m.menuType === 1)
    }
  } catch {}
}

function flattenMenus(tree: any[]): any[] {
  const result: any[] = []
  for (const item of tree) {
    result.push(item)
    if (item.children?.length) {
      result.push(...flattenMenus(item.children))
    }
  }
  return result
}

function handleMenuBindChange(menuId: string) {
  // 保存到流程扩展信息（可通过后端API持久化）
  if (!modeler) return
  const root = modeler.get('canvas').getRootElement()
  const bo = root.businessObject || root
  bo.set('flowable:menuId', menuId || undefined)
}

// 默认流选项（用于网关）
const defaultFlowOptions = computed(() => {
  if (!selectedElement.value || !isGateway.value) return []
  const bo = selectedElement.value.businessObject || selectedElement.value
  const outgoing = bo.get('outgoing') || []
  return outgoing.map((f: any) => ({
    label: f.targetRef?.name || f.targetRef?.id || f.id,
    value: f.id
  }))
})

// 多实例配置
const multiInstanceData = ref<any>(null)

// ==================== 默认 BPMN XML ====================

const DEFAULT_XML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://bpmn.io/schema/bpmn"
  id="Definitions_${Date.now()}">
  <bpmn:process id="process_${Date.now()}" name="测试流程" isExecutable="true" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="process_${Date.now()}" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

// ==================== 初始化 ====================

// 抑制 bpmn-js 弃用警告（ContextPad#getPad）
const originalWarn = console.warn
console.warn = (...args) => {
  if (args[0]?.includes?.('deprecated') && args[0]?.includes?.('ContextPad')) return
  originalWarn.apply(console, args)
}

onMounted(async () => {
  await nextTick()
  initModeler()

  document.addEventListener('keydown', handleKeydown)

  if (props.xml) {
    await importXml(props.xml)
  } else if (props.modelId) {
    await loadModel(props.modelId)
  } else {
    await importXml(DEFAULT_XML)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (modeler) {
    modeler.destroy()
    modeler = null
  }
})

// ==================== 快捷键 ====================

function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    handleSave()
  }
}

// ==================== Modeler 初始化 ====================

function initModeler() {
  if (!canvasRef.value) return

  modeler = new BpmnModeler({
    container: canvasRef.value,
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      FlowablePropertiesProvider,
      MinimapModule,
      translateModule
    ],
    moddleExtensions: {
      flowable: flowableDescriptor
    }
  })

  // 默认任务类型为用户任务
  const eventBus = modeler.get('eventBus')
  const bpmnReplace = modeler.get('bpmnReplace')
  eventBus.on('create.end', (event: any) => {
    const element = event.element || event.shape
    if (element && element.type === 'bpmn:Task') {
      try {
        bpmnReplace.replaceElement(element, { type: 'bpmn:UserTask' })
      } catch {}
    }
  })

  // 监听选中元素变化
  const selection = modeler.get('selection')

  eventBus.on('selection.changed', ({ newSelection }: { newSelection: any[] }) => {
    if (newSelection.length > 0) {
      const element = newSelection[0]
      selectedElement.value = element
      selectedType.value = element.type || ''
      loadListenersFromElement(element)
      loadMultiInstanceFromElement(element)
    } else {
      // 选中画布（流程级别）
      const root = modeler!.get('canvas').getRootElement()
      selectedElement.value = root
      selectedType.value = 'bpmn:Process'
      loadListenersFromElement(root)
      loadMultiInstanceFromElement(root)
    }
  })

  // 自动放置
  eventBus.on('autoPlace.end', ({ source, shape }: { source: any; shape: any }) => {
    if (shape.type === 'bpmn:UserTask') {
      if (source.type === 'bpmn:StartEvent') {
        shape.businessObject.assignee = '${initiator}'
      }
    }
  })

  // 加载菜单列表
  loadMenuList()
}

// ==================== 监听器数据同步 ====================

function loadListenersFromElement(element: any) {
  executionListeners.value = []
  taskListeners.value = []

  if (!element) return

  const bo = element.businessObject || element
  if (!bo || typeof bo.get !== 'function') return

  const extensionElements = bo.get('extensionElements')
  if (!extensionElements) return

  const values = extensionElements.get('values') || []
  executionListeners.value = values.filter((e: any) => e.$instanceOf('flowable:ExecutionListener'))
  taskListeners.value = values.filter((e: any) => e.$instanceOf('flowable:TaskListener'))
}

function updateElementListeners(type: 'execution' | 'task', listeners: any[]) {
  if (!selectedElement.value || !modeler) return

  const bo = selectedElement.value.businessObject || selectedElement.value
  if (!bo || typeof bo.get !== 'function') return

  const bpmnFactory = modeler.get('bpmnFactory')
  const commandStack = modeler.get('commandStack')

  let extensionElements = bo.get('extensionElements')
  if (!extensionElements) {
    extensionElements = bpmnFactory.create('bpmn:ExtensionElements', { values: [] })
  }

  // 移除旧的同类型监听器
  const existing = extensionElements.get('values') || []
  const typeName = type === 'execution' ? 'flowable:ExecutionListener' : 'flowable:TaskListener'
  const filtered = existing.filter((e: any) => !e.$instanceOf(typeName))

  // 添加新的监听器
  extensionElements.set('values', [...filtered, ...listeners])

  commandStack.execute('element.updateProperties', {
    element: selectedElement.value,
    properties: { extensionElements }
  })

  // 重新加载
  loadListenersFromElement(selectedElement.value)
}

function createListenerElement(formData: any, type: 'execution' | 'task') {
  if (!modeler) return null
  const bpmnFactory = modeler.get('bpmnFactory')
  const typeName = type === 'execution' ? 'flowable:ExecutionListener' : 'flowable:TaskListener'

  const props: any = { event: formData.event }
  if (formData.type === 'class') props.class = formData.value
  else if (formData.type === 'expression') props.expression = formData.value
  else if (formData.type === 'delegateExpression') props.delegateExpression = formData.value

  if (formData.fields?.length) {
    props.fields = formData.fields.map((f: any) => {
      const field = bpmnFactory.create('flowable:Field', { name: f.name })
      if (f.type === 'expression' || f.value?.startsWith('${') || f.value?.startsWith('#{')) {
        field.expression = f.value
      } else {
        field.stringValue = f.value
      }
      return field
    })
  }

  return bpmnFactory.create(typeName, props)
}

function handleListenerAdd(type: 'execution' | 'task', formData: any) {
  const el = createListenerElement(formData, type)
  if (!el) return
  const current = type === 'execution' ? executionListeners.value : taskListeners.value
  updateElementListeners(type, [...current, el])
}

function handleListenerEdit(type: 'execution' | 'task', index: number, formData: any) {
  const el = createListenerElement(formData, type)
  if (!el) return
  const current = [...(type === 'execution' ? executionListeners.value : taskListeners.value)]
  current[index] = el
  updateElementListeners(type, current)
}

function handleListenerDelete(type: 'execution' | 'task', index: number) {
  const current = [...(type === 'execution' ? executionListeners.value : taskListeners.value)]
  current.splice(index, 1)
  updateElementListeners(type, current)
}

// ==================== 多实例配置 ====================

function loadMultiInstanceFromElement(element: any) {
  multiInstanceData.value = null
  if (!element) return
  const bo = element.businessObject || element
  if (!bo || typeof bo.get !== 'function') return
  const li = bo.get('loopCharacteristics')
  if (!li) return
  multiInstanceData.value = {
    isSequential: li.isSequential || false,
    completionCondition: li.completionCondition?.body || '',
    collection: li.collection || '',
    elementVariable: li.elementVariable || ''
  }
}

function handleMultiInstanceUpdate(data: any) {
  if (!selectedElement.value || !modeler) return
  const bpmnFactory = modeler.get('bpmnFactory')
  const commandStack = modeler.get('commandStack')
  const bo = selectedElement.value.businessObject || selectedElement.value

  if (!data) {
    // 移除多实例
    commandStack.execute('element.updateProperties', {
      element: selectedElement.value,
      properties: { loopCharacteristics: undefined }
    })
    multiInstanceData.value = null
    return
  }

  let li = bo.get('loopCharacteristics')
  if (!li) {
    li = bpmnFactory.create('bpmn:MultiInstanceLoopCharacteristics')
  }
  li.isSequential = data.isSequential
  if (data.collection) li.collection = data.collection
  else li.collection = undefined
  if (data.elementVariable) li.elementVariable = data.elementVariable
  else li.elementVariable = undefined
  if (data.completionCondition) {
    if (!li.completionCondition) {
      li.completionCondition = bpmnFactory.create('bpmn:Expression', { body: data.completionCondition })
    } else {
      li.completionCondition.body = data.completionCondition
    }
  } else {
    li.completionCondition = undefined
  }

  commandStack.execute('element.updateProperties', {
    element: selectedElement.value,
    properties: { loopCharacteristics: li }
  })
  multiInstanceData.value = data
}

// ==================== 定时器配置 ====================

function handleTimerUpdate(timerData: any) {
  if (!selectedElement.value || !modeler) return
  const bpmnFactory = modeler.get('bpmnFactory')
  const commandStack = modeler.get('commandStack')
  const bo = selectedElement.value.businessObject || selectedElement.value

  let eventDefinitions = bo.get('eventDefinitions') || []
  // 移除旧的 TimerEventDefinition
  eventDefinitions = eventDefinitions.filter((e: any) => !e.$instanceOf('bpmn:TimerEventDefinition'))

  if (timerData) {
    const timerDef = bpmnFactory.create('bpmn:TimerEventDefinition')
    if (timerData.type === 'date') {
      timerDef.timeDate = bpmnFactory.create('bpmn:Expression', { body: timerData.value })
    } else if (timerData.type === 'duration') {
      timerDef.timeDuration = bpmnFactory.create('bpmn:Expression', { body: timerData.value })
    } else if (timerData.type === 'cycle') {
      timerDef.timeCycle = bpmnFactory.create('bpmn:Expression', { body: timerData.value })
    }
    eventDefinitions.push(timerDef)
  }

  commandStack.execute('element.updateProperties', {
    element: selectedElement.value,
    properties: { eventDefinitions }
  })
}

// ==================== 网关默认流 ====================

function handleDefaultFlowUpdate(flowId: string) {
  if (!selectedElement.value || !modeler) return
  const commandStack = modeler.get('commandStack')
  commandStack.execute('element.updateProperties', {
    element: selectedElement.value,
    properties: { default: flowId || undefined }
  })
}

// ==================== 字段权限 ====================

function handleFormFieldPermission() {
  // 弹窗由 FormFieldPermission 组件内部处理
}

// ==================== 属性更新 ====================

function updateProperty(name: string, value: any) {
  if (!selectedElement.value || !modeler) return

  const bo = selectedElement.value.businessObject || selectedElement.value
  if (!bo || typeof bo.get !== 'function') return

  const commandStack = modeler.get('commandStack')

  if (name.startsWith('flowable:')) {
    bo.set(name, value || undefined)
  } else {
    commandStack.execute('element.updateProperties', {
      element: selectedElement.value,
      properties: { [name]: value }
    })
  }
}

function getPropertyValue(name: string): string {
  if (!selectedElement.value) return ''
  const bo = selectedElement.value.businessObject || selectedElement.value
  if (!bo || typeof bo.get !== 'function') return ''
  if (name.startsWith('flowable:')) {
    return bo.get(name) || ''
  }
  return bo[name] || ''
}

// ==================== 加载模型 ====================

async function importXml(xml: string) {
  if (!modeler) return
  try {
    const { warnings } = await modeler.importXML(xml)
    if (warnings.length) {
      console.warn('导入警告:', warnings)
    }
    const canvas = modeler.get('canvas')
    canvas.zoom('fit-viewport')
    updateZoom()
  } catch (err) {
    console.error('导入 XML 失败:', err)
    ElMessage.error('导入流程图失败')
  }
}

async function loadModel(modelId: string) {
  loading.value = true
  try {
    const res = await getBpmnByModelId(modelId)
    // bpmnRequest 返回 res 直接（已处理 code 检查），data 在 res.data 中
    const modelXml = res.data?.modelXml
    if (modelXml) {
      await importXml(modelXml)
    } else {
      ElMessage.warning('模型数据为空，加载空白流程')
      await importXml(DEFAULT_XML)
    }
  } catch (err) {
    console.error('加载模型失败:', err)
    ElMessage.error('加载模型失败')
    await importXml(DEFAULT_XML)
  } finally {
    loading.value = false
  }
}

// ==================== 保存 ====================

async function handleSave() {
  if (!modeler) return
  saving.value = true
  try {
    const { xml } = await modeler.saveXML({ format: true })
    const processElement = modeler.get('canvas').getRootElement().businessObject
    const processId = processElement.id
    const processName = processElement.name || '未命名流程'

    const data = {
      modelId: props.modelId || '',
      modelKey: processId,
      modelName: processName,
      modelXml: xml,
      fileName: processName
    }

    const res = await saveBpmnModel(data)
    // bpmnRequest 返回 res 直接，data 在 res.data 中
    const savedModelId = res.data

    ElMessage.success('保存成功')
    emit('save', xml!, savedModelId)
  } catch (err) {
    console.error('保存失败:', err)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// ==================== 导入文件 ====================

function handleImport() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.bpmn,.xml'
  input.onchange = async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = async (ev) => {
      const xml = ev.target?.result as string
      if (xml) {
        await importXml(xml)
        ElMessage.success('导入成功')
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

// ==================== 预览 XML ====================

async function handlePreview() {
  if (!modeler) return
  try {
    const { xml } = await modeler.saveXML({ format: true })
    previewXml.value = xml || ''
    previewVisible.value = true
  } catch (err) {
    ElMessage.error('生成 XML 失败')
  }
}

// ==================== 清空画布 ====================

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空画布？此操作不可撤销。', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await importXml(DEFAULT_XML)
    ElMessage.success('已清空')
  } catch {
    // 用户取消
  }
}

// ==================== 工具栏操作 ====================

function handleUndo() {
  modeler?.get('commandStack').undo()
}

function handleRedo() {
  modeler?.get('commandStack').redo()
}

function handleZoomIn() {
  if (modeler) {
    const canvas = modeler.get('canvas')
    const newZoom = Math.min(zoom.value + 10, 200)
    canvas.zoom(newZoom / 100)
    zoom.value = newZoom
  }
}

function handleZoomOut() {
  if (modeler) {
    const canvas = modeler.get('canvas')
    const newZoom = Math.max(zoom.value - 10, 30)
    canvas.zoom(newZoom / 100)
    zoom.value = newZoom
  }
}

function handleFitViewport() {
  if (modeler) {
    const canvas = modeler.get('canvas')
    canvas.zoom('fit-viewport')
    updateZoom()
  }
}

function toggleMinimap() {
  if (modeler) {
    const minimap = modeler.get('minimap', false)
    if (minimap) {
      minimap.toggle()
      minimapOpen.value = !minimapOpen.value
    }
  }
}

function updateZoom() {
  if (modeler) {
    const canvas = modeler.get('canvas')
    zoom.value = Math.round((canvas.zoom() || 1) * 100)
  }
}

// ==================== 对齐操作 ====================

function handleAlign(direction: string) {
  if (!modeler) return
  const selection = modeler.get('selection')
  const selectedElements = selection.get()
  if (selectedElements.length < 2) {
    ElMessage.warning('请选中至少两个元素')
    return
  }
  const modeling = modeler.get('modeling')
  modeling.alignElements(selectedElements, { alignment: direction })
}

// ==================== 部署 ====================

async function handleDeploy() {
  if (!modeler) return
  try {
    const { xml } = await modeler.saveXML({ format: true })
    const processElement = modeler.get('canvas').getRootElement().businessObject
    const processId = processElement.id
    const processName = processElement.name || '未命名流程'

    const data = {
      modelId: props.modelId || '',
      modelKey: processId,
      modelName: processName,
      modelXml: xml,
      fileName: processName
    }

    // 先保存，再部署
    const res = await saveBpmnModel(data)
    // bpmnRequest 返回 res 直接，data 在 res.data 中
    const savedModelId = res.data

    if (savedModelId) {
      ElMessage.success('部署成功')
      emit('deploy', savedModelId)
    }
  } catch (err) {
    console.error('部署失败:', err)
    ElMessage.error('部署失败')
  }
}

async function handleDownload() {
  if (!modeler) return
  try {
    const { xml } = await modeler.saveXML({ format: true })
    const blob = new Blob([xml!], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `process_${Date.now()}.bpmn20.xml`
    a.click()
    URL.revokeObjectURL(url)
  } catch (err) {
    ElMessage.error('导出失败')
  }
}
</script>

<template>
  <div class="process-designer" v-loading="loading">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-tooltip content="保存 (Ctrl+S)" placement="bottom">
          <el-button type="primary" :loading="saving" :disabled="props.readonly" @click="handleSave">
            保存
          </el-button>
        </el-tooltip>

        <el-divider direction="vertical" />

        <el-tooltip content="导入 BPMN 文件" placement="bottom">
          <el-button :icon="ElIconUpload" @click="handleImport" />
        </el-tooltip>
        <el-tooltip content="预览 XML" placement="bottom">
          <el-button :icon="ElIconView" @click="handlePreview" />
        </el-tooltip>
        <el-tooltip content="下载 BPMN 文件" placement="bottom">
          <el-button :icon="ElIconDownload" @click="handleDownload" />
        </el-tooltip>
        <el-tooltip content="清空画布" placement="bottom">
          <el-button :icon="ElIconDelete" :disabled="props.readonly" @click="handleClear" />
        </el-tooltip>

        <el-divider direction="vertical" />

        <el-tooltip content="撤销" placement="bottom">
          <el-button :icon="ElIconUndo" :disabled="props.readonly" @click="handleUndo" />
        </el-tooltip>
        <el-tooltip content="重做" placement="bottom">
          <el-button :icon="ElIconRedo" :disabled="props.readonly" @click="handleRedo" />
        </el-tooltip>

        <el-divider direction="vertical" />

        <el-tooltip content="放大" placement="bottom">
          <el-button :icon="ElIconZoomIn" @click="handleZoomIn" />
        </el-tooltip>
        <span class="zoom-value">{{ zoom }}%</span>
        <el-tooltip content="缩小" placement="bottom">
          <el-button :icon="ElIconZoomOut" @click="handleZoomOut" />
        </el-tooltip>
        <el-tooltip content="适应视口" placement="bottom">
          <el-button @click="handleFitViewport">适应</el-button>
        </el-tooltip>

        <el-divider direction="vertical" />

        <el-tooltip content="小地图" placement="bottom">
          <el-button :icon="ElIconMap" :class="{ 'is-active': minimapOpen }" @click="toggleMinimap" />
        </el-tooltip>

        <el-divider direction="vertical" />

        <!-- 对齐工具 -->
        <el-dropdown trigger="click">
          <el-button>对齐</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleAlign('left')">左对齐</el-dropdown-item>
              <el-dropdown-item @click="handleAlign('center')">水平居中</el-dropdown-item>
              <el-dropdown-item @click="handleAlign('right')">右对齐</el-dropdown-item>
              <el-dropdown-item divided @click="handleAlign('top')">上对齐</el-dropdown-item>
              <el-dropdown-item @click="handleAlign('middle')">垂直居中</el-dropdown-item>
              <el-dropdown-item @click="handleAlign('bottom')">下对齐</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="toolbar-right">
        <el-tooltip content="部署流程" placement="bottom">
          <el-button type="success" :disabled="props.readonly" @click="handleDeploy">
            部署
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 画布 + 属性面板 -->
    <div class="designer-body">
      <div ref="canvasRef" class="canvas-container" />

      <div class="right-panel">
        <div class="panel-title">
          <span>{{ selectedType === 'bpmn:Process' ? '流程属性' : '节点属性' }}</span>
          <span class="panel-type">{{ selectedType?.replace('bpmn:', '') }}</span>
        </div>

        <div class="panel-scroll">
          <!-- 基本信息（所有节点） -->
          <PropertyGroup title="基本信息" :model-value="groupOpen.basic" @update:model-value="groupOpen.basic = $event">
            <div class="prop-row">
              <div class="prop-label">ID</div>
              <el-input size="small" :model-value="getPropertyValue('id')" disabled />
            </div>
            <div class="prop-row">
              <div class="prop-label">名称</div>
              <el-input size="small" :model-value="getPropertyValue('name')" @update:model-value="val => updateProperty('name', val)" placeholder="节点名称" />
            </div>
            <!-- 流程级别：归属菜单 -->
            <div v-if="isProcess" class="prop-row">
              <div class="prop-label">归属菜单</div>
              <el-select
                v-model="processMenuKey"
                clearable
                filterable
                placeholder="选择归属菜单"
                size="small"
                style="width: 100%"
                @change="handleMenuBindChange"
              >
                <el-option
                  v-for="menu in menuList"
                  :key="menu.id"
                  :label="menu.menuName"
                  :value="menu.id"
                />
              </el-select>
            </div>
            <!-- 流程级别：表单绑定 -->
            <div v-if="isProcess" class="prop-row">
              <div class="prop-label">绑定表单</div>
              <FormTagInput
                :model-value="getPropertyValue('flowable:formKey')"
                @update:model-value="val => updateProperty('flowable:formKey', val)"
                placeholder="选择绑定的表单"
              />
            </div>
          </PropertyGroup>

          <!-- 审批人配置（UserTask） -->
          <PropertyGroup v-if="isUserTask" title="审批人配置" :model-value="groupOpen.assignee" @update:model-value="groupOpen.assignee = $event">
            <div class="prop-row">
              <div class="prop-label">处理人</div>
              <UserTagInput :model-value="getPropertyValue('flowable:assignee')" @update:model-value="val => updateProperty('flowable:assignee', val)" placeholder="选择处理人或输入表达式" />
            </div>
            <div class="prop-row">
              <div class="prop-label">候选用户</div>
              <UserTagInput :model-value="getPropertyValue('flowable:candidateUsers')" @update:model-value="val => updateProperty('flowable:candidateUsers', val)" :multiple="true" placeholder="选择候选用户" />
            </div>
            <div class="prop-row">
              <div class="prop-label">候选角色</div>
              <RoleTagInput :model-value="getPropertyValue('flowable:candidateGroups')" @update:model-value="val => updateProperty('flowable:candidateGroups', val)" :multiple="true" placeholder="选择候选角色" />
            </div>
          </PropertyGroup>

          <!-- 表单字段权限（UserTask） -->
          <PropertyGroup v-if="isUserTask && getPropertyValue('flowable:formKey')" title="表单字段权限" :model-value="groupOpen.form" @update:model-value="groupOpen.form = $event">
            <div class="prop-row">
              <div class="prop-label">字段权限</div>
              <FormFieldPermission :process-key="processKey" :activity-id="getPropertyValue('id')" :form-code="getPropertyValue('flowable:formKey')" />
            </div>
          </PropertyGroup>

          <!-- 服务任务配置 -->
          <PropertyGroup v-if="isServiceTask" title="服务配置" :model-value="groupOpen.service" @update:model-value="groupOpen.service = $event">
            <div class="prop-row">
              <div class="prop-label">Java 类</div>
              <el-input size="small" :model-value="getPropertyValue('flowable:class')" @update:model-value="val => updateProperty('flowable:class', val)" placeholder="com.example.MyDelegate" />
            </div>
            <div class="prop-row">
              <div class="prop-label">委托表达式</div>
              <el-input size="small" :model-value="getPropertyValue('flowable:delegateExpression')" @update:model-value="val => updateProperty('flowable:delegateExpression', val)" placeholder="${myDelegateBean}" />
            </div>
            <div class="prop-row">
              <div class="prop-label">表达式</div>
              <el-input size="small" :model-value="getPropertyValue('flowable:expression')" @update:model-value="val => updateProperty('flowable:expression', val)" placeholder="${myService.execute()}" />
            </div>
            <div class="prop-row">
              <div class="prop-label">结果变量</div>
              <el-input size="small" :model-value="getPropertyValue('flowable:resultVariable')" @update:model-value="val => updateProperty('flowable:resultVariable', val)" placeholder="result" />
            </div>
          </PropertyGroup>

          <!-- 脚本任务配置 -->
          <PropertyGroup v-if="isScriptTask" title="脚本配置" :model-value="groupOpen.script" @update:model-value="groupOpen.script = $event">
            <div class="prop-row">
              <div class="prop-label">脚本格式</div>
              <el-input size="small" :model-value="getPropertyValue('scriptFormat')" @update:model-value="val => updateProperty('scriptFormat', val)" placeholder="groovy / javascript" />
            </div>
            <div class="prop-row">
              <div class="prop-label">脚本内容</div>
              <el-input type="textarea" :rows="4" size="small" :model-value="getPropertyValue('script')" @update:model-value="val => updateProperty('script', val)" placeholder="脚本代码" />
            </div>
            <div class="prop-row">
              <div class="prop-label">结果变量</div>
              <el-input size="small" :model-value="getPropertyValue('flowable:resultVariable')" @update:model-value="val => updateProperty('flowable:resultVariable', val)" placeholder="result" />
            </div>
          </PropertyGroup>

          <!-- 多实例配置（UserTask / SubProcess） -->
          <PropertyGroup v-if="isUserTask || isSubProcess" title="多实例配置" :model-value="groupOpen.multiInstance" @update:model-value="groupOpen.multiInstance = $event">
            <MultiInstanceConfig :data="multiInstanceData" @update="handleMultiInstanceUpdate" />
          </PropertyGroup>

          <!-- 定时器配置（StartEvent / TimerEvent） -->
          <PropertyGroup v-if="isTimerEvent" title="定时器配置" :model-value="groupOpen.timer" @update:model-value="groupOpen.timer = $event">
            <TimerConfig :element="selectedElement" @update="handleTimerUpdate" />
          </PropertyGroup>

          <!-- 网关配置 -->
          <PropertyGroup v-if="isGateway" title="网关配置" :model-value="groupOpen.gateway" @update:model-value="groupOpen.gateway = $event">
            <div class="prop-row">
              <div class="prop-label">默认流</div>
              <el-select size="small" :model-value="getPropertyValue('default')" @update:model-value="val => handleDefaultFlowUpdate(val)" placeholder="无" clearable style="width: 100%">
                <el-option v-for="opt in defaultFlowOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>
          </PropertyGroup>

          <!-- 异步配置（所有节点，Process 除外） -->
          <PropertyGroup v-if="!isProcess" title="异步配置" :model-value="groupOpen.async" @update:model-value="groupOpen.async = $event">
            <div class="prop-row prop-row-inline">
              <el-checkbox :model-value="getPropertyValue('flowable:async') === 'true'" @update:model-value="val => updateProperty('flowable:async', val ? 'true' : undefined)">异步执行</el-checkbox>
            </div>
            <div class="prop-row prop-row-inline">
              <el-checkbox :model-value="getPropertyValue('flowable:exclusive') === 'true'" @update:model-value="val => updateProperty('flowable:exclusive', val ? 'true' : undefined)">排他</el-checkbox>
            </div>
          </PropertyGroup>

          <!-- 调用活动配置 -->
          <PropertyGroup v-if="isCallActivity" title="调用活动配置" :model-value="groupOpen.callActivity" @update:model-value="groupOpen.callActivity = $event">
            <div class="prop-row">
              <div class="prop-label">调用流程Key</div>
              <el-input size="small" :model-value="getPropertyValue('calledElement')" @update:model-value="val => updateProperty('calledElement', val)" placeholder="被调用的流程Key" />
            </div>
          </PropertyGroup>

          <!-- 任务监听器（UserTask） -->
          <PropertyGroup v-if="isUserTask" title="任务监听器" :model-value="groupOpen.taskListener" @update:model-value="groupOpen.taskListener = $event">
            <ListenerManager :listeners="taskListeners" type="task" @add="data => handleListenerAdd('task', data)" @edit="(i, data) => handleListenerEdit('task', i, data)" @delete="i => handleListenerDelete('task', i)" />
          </PropertyGroup>

          <!-- 执行监听器（所有节点） -->
          <PropertyGroup title="执行监听器" :model-value="groupOpen.executionListener" @update:model-value="groupOpen.executionListener = $event">
            <ListenerManager :listeners="executionListeners" type="execution" @add="data => handleListenerAdd('execution', data)" @edit="(i, data) => handleListenerEdit('execution', i, data)" @delete="i => handleListenerDelete('execution', i)" />
          </PropertyGroup>
        </div>
      </div>
    </div>

    <!-- XML 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="BPMN XML 预览" width="70%" top="5vh">
      <pre class="xml-preview">{{ previewXml }}</pre>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.process-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: #fafafa;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
  min-height: 40px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar :deep(.el-button) {
  padding: 6px 10px;
}

.toolbar :deep(.el-button--primary) {
  padding: 6px 16px;
}

.zoom-value {
  display: inline-block;
  min-width: 44px;
  text-align: center;
  font-size: 13px;
  color: #606266;
}

.is-active {
  background-color: #ecf5ff;
  color: #409eff;
}

/* 画布 + 右侧面板 */
.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.canvas-container {
  flex: 1;
  position: relative;
  background: #fff;
}

.right-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e4e7ed;
  background: #fff;
  overflow: hidden;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.panel-type {
  font-size: 11px;
  font-weight: 400;
  color: #909399;
  background: #e8e8e8;
  padding: 2px 6px;
  border-radius: 3px;
}

.panel-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.prop-row {
  margin-bottom: 10px;
}

.prop-row:last-child {
  margin-bottom: 0;
}

.prop-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  font-weight: 500;
}

.prop-row-inline {
  display: flex;
  align-items: center;
}

/* bpmn-js 默认样式覆盖 */
.canvas-container :deep(.bjs-powered-by) {
  display: none;
}

/* 隐藏标准属性面板（使用自定义面板） */
:deep(.bio-properties-panel) {
  display: none !important;
}

/* 小地图样式 */
.canvas-container :deep(.djs-minimap) {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* XML 预览 */
.xml-preview {
  max-height: 60vh;
  overflow: auto;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
