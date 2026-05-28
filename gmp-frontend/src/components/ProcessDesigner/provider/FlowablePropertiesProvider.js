/**
 * Flowable Properties Provider
 *
 * 为 bpmn-js-properties-panel 提供 Flowable 特有的属性分组
 * 使用 propertiesPanel.registerProvider 机制扩展属性面板
 */

import { is } from 'bpmn-js/lib/util/ModelUtil'

const LOW_PRIORITY = 500

export default class FlowablePropertiesProvider {
  constructor(propertiesPanel, injector, translate) {
    propertiesPanel.registerProvider(LOW_PRIORITY, this)
    this._injector = injector
    this._translate = translate
  }

  getGroups(element) {
    return (groups) => {
      // 移除 camunda 特有的组（如果存在）
      groups = groups.filter(g => !g.id.startsWith('CamundaPlatform_'))

      // 根据节点类型添加 Flowable 属性组
      if (is(element, 'bpmn:Process') || is(element, 'bpmn:Collaboration')) {
        const processGroup = createProcessGroup(element, this._translate)
        if (processGroup) groups.push(processGroup)
      }

      if (is(element, 'bpmn:UserTask')) {
        const userTaskGroup = createUserTaskGroup(element, this._translate)
        if (userTaskGroup) groups.push(userTaskGroup)
      }

      if (is(element, 'bpmn:ServiceTask')) {
        const serviceTaskGroup = createServiceTaskGroup(element, this._translate)
        if (serviceTaskGroup) groups.push(serviceTaskGroup)
      }

      if (is(element, 'bpmn:ScriptTask')) {
        const scriptTaskGroup = createScriptTaskGroup(element, this._translate)
        if (scriptTaskGroup) groups.push(scriptTaskGroup)
      }

      // 执行监听器（适用于大多数节点）
      if (is(element, 'bpmn:Activity') || is(element, 'bpmn:Event') ||
          is(element, 'bpmn:Gateway') || is(element, 'bpmn:SequenceFlow')) {
        const listenerGroup = createExecutionListenerGroup(element, this._translate)
        if (listenerGroup) groups.push(listenerGroup)
      }

      // 任务监听器（仅适用于用户任务）
      if (is(element, 'bpmn:UserTask')) {
        const taskListenerGroup = createTaskListenerGroup(element, this._translate)
        if (taskListenerGroup) groups.push(taskListenerGroup)
      }

      return groups
    }
  }
}

FlowablePropertiesProvider.$inject = ['propertiesPanel', 'injector', 'translate']

// ==================== 工具函数 ====================

function getBusinessObject(element) {
  return element.businessObject || element
}

function getExtensionElements(businessObject, type) {
  const extensionElements = businessObject.get('extensionElements')
  if (!extensionElements) return []
  return extensionElements.get('values').filter(e => !type || e.$instanceOf(type))
}

// ==================== 属性条目工厂 ====================

/**
 * 创建文本输入条目
 */
function createTextEntry(options) {
  const { id, label, modelProperty, get, set } = options
  return {
    id,
    label,
    modelProperty,
    get: get || (() => ({})),
    set: set || (() => ({})),
    validate: () => ({})
  }
}

// ==================== Process 属性组 ====================

function createProcessGroup(element, translate) {
  const bo = getBusinessObject(element)

  return {
    id: 'flowableProcess',
    label: translate('流程属性'),
    entries: [
      createTextEntry({
        id: 'processId',
        label: translate('流程 ID'),
        modelProperty: 'id',
        get: (e) => ({ id: getBusinessObject(e).id }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          return { id: { ...bo, id: values.id } }
        }
      }),
      createTextEntry({
        id: 'processName',
        label: translate('流程名称'),
        modelProperty: 'name',
        get: (e) => ({ name: getBusinessObject(e).name || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          return { name: { ...bo, name: values.name } }
        }
      }),
      createTextEntry({
        id: 'isExecutable',
        label: translate('可执行'),
        modelProperty: 'isExecutable',
        get: (e) => ({ isExecutable: String(getBusinessObject(e).isExecutable !== false) }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          return { isExecutable: { ...bo, isExecutable: values.isExecutable === 'true' } }
        }
      })
    ],
    isOpen: true
  }
}

// ==================== UserTask 属性组 ====================

function createUserTaskGroup(element, translate) {
  const bo = getBusinessObject(element)

  return {
    id: 'flowableUserTask',
    label: translate('用户任务属性'),
    entries: [
      createTextEntry({
        id: 'assignee',
        label: translate('处理人'),
        modelProperty: 'assignee',
        get: (e) => ({ assignee: getBusinessObject(e).get('flowable:assignee') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:assignee', values.assignee || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'candidateUsers',
        label: translate('候选用户'),
        modelProperty: 'candidateUsers',
        get: (e) => ({ candidateUsers: getBusinessObject(e).get('flowable:candidateUsers') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:candidateUsers', values.candidateUsers || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'candidateGroups',
        label: translate('候选角色'),
        modelProperty: 'candidateGroups',
        get: (e) => ({ candidateGroups: getBusinessObject(e).get('flowable:candidateGroups') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:candidateGroups', values.candidateGroups || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'formKey',
        label: translate('表单 Key'),
        modelProperty: 'formKey',
        get: (e) => ({ formKey: getBusinessObject(e).get('flowable:formKey') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:formKey', values.formKey || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'dueDate',
        label: translate('到期时间'),
        modelProperty: 'dueDate',
        get: (e) => ({ dueDate: getBusinessObject(e).get('flowable:dueDate') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:dueDate', values.dueDate || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'priority',
        label: translate('优先级'),
        modelProperty: 'priority',
        get: (e) => ({ priority: getBusinessObject(e).get('flowable:priority') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:priority', values.priority || undefined)
          return {}
        }
      })
    ],
    isOpen: true
  }
}

// ==================== ServiceTask 属性组 ====================

function createServiceTaskGroup(element, translate) {
  const bo = getBusinessObject(element)

  return {
    id: 'flowableServiceTask',
    label: translate('服务任务属性'),
    entries: [
      createTextEntry({
        id: 'serviceTaskClass',
        label: translate('Java 类'),
        modelProperty: 'class',
        get: (e) => ({ class: getBusinessObject(e).get('flowable:class') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:class', values.class || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'delegateExpression',
        label: translate('委托表达式'),
        modelProperty: 'delegateExpression',
        get: (e) => ({ delegateExpression: getBusinessObject(e).get('flowable:delegateExpression') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:delegateExpression', values.delegateExpression || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'expression',
        label: translate('表达式'),
        modelProperty: 'expression',
        get: (e) => ({ expression: getBusinessObject(e).get('flowable:expression') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:expression', values.expression || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'resultVariable',
        label: translate('结果变量'),
        modelProperty: 'resultVariable',
        get: (e) => ({ resultVariable: getBusinessObject(e).get('flowable:resultVariable') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:resultVariable', values.resultVariable || undefined)
          return {}
        }
      })
    ],
    isOpen: true
  }
}

// ==================== ScriptTask 属性组 ====================

function createScriptTaskGroup(element, translate) {
  const bo = getBusinessObject(element)

  return {
    id: 'flowableScriptTask',
    label: translate('脚本任务属性'),
    entries: [
      createTextEntry({
        id: 'script',
        label: translate('脚本内容'),
        modelProperty: 'script',
        get: (e) => ({ script: getBusinessObject(e).get('flowable:script') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:script', values.script || undefined)
          return {}
        }
      }),
      createTextEntry({
        id: 'resultVariable',
        label: translate('结果变量'),
        modelProperty: 'resultVariable',
        get: (e) => ({ resultVariable: getBusinessObject(e).get('flowable:resultVariable') || '' }),
        set: (e, values) => {
          const bo = getBusinessObject(e)
          bo.set('flowable:resultVariable', values.resultVariable || undefined)
          return {}
        }
      })
    ],
    isOpen: true
  }
}

// ==================== 执行监听器组 ====================

function createExecutionListenerGroup(element, translate) {
  const bo = getBusinessObject(element)
  const listeners = getExtensionElements(bo, 'flowable:ExecutionListener')

  if (listeners.length === 0) return null

  return {
    id: 'flowableExecutionListeners',
    label: translate('执行监听器'),
    entries: listeners.map((listener, idx) =>
      createTextEntry({
        id: `execListener_${idx}`,
        label: `${listener.event || ''} - ${listener.class || listener.expression || listener.delegateExpression || ''}`,
        modelProperty: 'event',
        get: () => ({
          event: listener.event || '',
          class: listener.get('flowable:class') || '',
          expression: listener.get('flowable:expression') || '',
          delegateExpression: listener.get('flowable:delegateExpression') || ''
        }),
        set: (e, values) => {
          listener.event = values.event
          listener.set('flowable:class', values.class || undefined)
          listener.set('flowable:expression', values.expression || undefined)
          listener.set('flowable:delegateExpression', values.delegateExpression || undefined)
          return {}
        }
      })
    ),
    isOpen: true
  }
}

// ==================== 任务监听器组 ====================

function createTaskListenerGroup(element, translate) {
  const bo = getBusinessObject(element)
  const listeners = getExtensionElements(bo, 'flowable:TaskListener')

  if (listeners.length === 0) return null

  return {
    id: 'flowableTaskListeners',
    label: translate('任务监听器'),
    entries: listeners.map((listener, idx) =>
      createTextEntry({
        id: `taskListener_${idx}`,
        label: `${listener.event || ''} - ${listener.class || listener.expression || listener.delegateExpression || ''}`,
        modelProperty: 'event',
        get: () => ({
          event: listener.event || '',
          class: listener.get('flowable:class') || '',
          expression: listener.get('flowable:expression') || '',
          delegateExpression: listener.get('flowable:delegateExpression') || ''
        }),
        set: (e, values) => {
          listener.event = values.event
          listener.set('flowable:class', values.class || undefined)
          listener.set('flowable:expression', values.expression || undefined)
          listener.set('flowable:delegateExpression', values.delegateExpression || undefined)
          return {}
        }
      })
    ),
    isOpen: true
  }
}
