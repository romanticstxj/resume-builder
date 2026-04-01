<template>
  <div class="rich-editor">
    <!-- Toolbar -->
    <div class="toolbar">
      <button type="button" :class="{ active: editor?.isActive('bulletList') }" @click="editor?.chain().focus().toggleBulletList().run()" title="无序列表">•</button>
      <button type="button" :class="{ active: editor?.isActive('orderedList') }" @click="editor?.chain().focus().toggleOrderedList().run()" title="有序列表">1.</button>
      <span class="sep" />
      <button type="button" :class="{ active: editor?.isActive({ textAlign: 'left' }) }" @click="editor?.chain().focus().setTextAlign('left').run()" title="左对齐">⬅</button>
      <button type="button" :class="{ active: editor?.isActive({ textAlign: 'center' }) }" @click="editor?.chain().focus().setTextAlign('center').run()" title="居中">☰</button>
      <button type="button" :class="{ active: editor?.isActive({ textAlign: 'right' }) }" @click="editor?.chain().focus().setTextAlign('right').run()" title="右对齐">➡</button>
    </div>
    <editor-content :editor="editor" class="editor-content" />
  </div>
</template>

<script setup>
import { watch, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import TextAlign from '@tiptap/extension-text-align'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

const editor = useEditor({
  content: props.modelValue || '',
  extensions: [
    StarterKit,
    TextAlign.configure({ types: ['heading', 'paragraph'] })
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  }
})

// Sync external value changes (e.g. when loading resume data)
watch(() => props.modelValue, (val) => {
  if (editor.value && val !== editor.value.getHTML()) {
    editor.value.commands.setContent(val || '', false)
  }
})

onBeforeUnmount(() => editor.value?.destroy())
</script>

<style scoped>
.rich-editor {
  border: 1px solid var(--td-border-level-2-color, #dcdcdc);
  border-radius: 4px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  background: #f9f9f9;
  border-bottom: 1px solid var(--td-border-level-2-color, #dcdcdc);
}

.toolbar button {
  width: 28px;
  height: 26px;
  border: none;
  background: transparent;
  border-radius: 3px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar button:hover { background: #e8e8e8; }
.toolbar button.active { background: #d0e4ff; color: var(--td-brand-color, #0052d9); }

.sep {
  width: 1px;
  height: 18px;
  background: #ddd;
  margin: 0 4px;
}

.editor-content {
  min-height: 80px;
  padding: 8px 10px;
  font-size: 14px;
  line-height: 1.6;
}

/* Tiptap content styles */
.editor-content :deep(.ProseMirror) {
  outline: none;
  min-height: 60px;
}

.editor-content :deep(ul) {
  padding-left: 20px;
  list-style-type: disc;
}

.editor-content :deep(ol) {
  padding-left: 20px;
  list-style-type: decimal;
}

.editor-content :deep(li) { margin: 2px 0; }
.editor-content :deep(p) { margin: 0 0 4px; }
</style>
