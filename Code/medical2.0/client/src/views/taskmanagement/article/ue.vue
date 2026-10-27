<template>
<div ref="editor" class="editor" :style="styles" />
</template>
<script>
import Quill from 'quill'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
export default {

name: 'Editor',
props: {

/* 编辑器的内容 */
value: {

type: String,
default: ''
},
/* chiều cao */
height: {

type: Number,
default: null
},
/* 最小高度 */
minHeight: {

type: Number,
default: null
},
disabled:[Boolean]
},
data() {

return {

Quill: null,
currentValue: '',
options: {

theme: 'snow',
bounds: document.body,
debug: 'warn',
modules: {

// Cấu hình thanh công cụ
toolbar: [
['bold', 'italic', 'underline', 'strike'], // In đậm Chữ nghiêng Gạch chân Gạch ngang chữ
//['blockquote', 'code-block'], // Trích dẫn Khối mã
[{
 list: 'ordered' }, {
 list: 'bullet' }], // Có thứ tự、Danh sách không thứ tự
[{
 indent: '-1' }, {
 indent: '+1' }], // Thụt lề
[{
 size: ['small', false, 'large', 'huge'] }], // Cỡ chữ
[{
 header: [1, 2, 3, 4, 5, 6, false] }], // Tiêu đề
[{
 color: [] }, {
 background: [] }], // Màu chữ、Màu nền chữ
[{
 align: [] }], // Cách căn chỉnh
['clean'], // Xóa định dạng văn bản
//['link', 'image', 'video'] // Liên kết、Hình ảnh、Video
[ 'image'] // Hình ảnh
]
},
placeholder: 'Vui lòng nhập nội dung',
readOnly: false //Chế độ chỉ đọc true
}
}
},
computed: {

styles() {

const style = {
}
if (this.minHeight) {

style.minHeight = `${
this.minHeight}px`
}
if (this.height) {

style.height = `${
this.height}px`
}
return style
}
},
watch: {

value: {

handler(val) {

if (val !== this.currentValue) {

this.currentValue = val === null ? '' : val
if (this.Quill) {

this.Quill.pasteHTML(this.currentValue)
}
}
},
immediate: true
}
},
mounted() {

this.init()
},
beforeDestroy() {

this.Quill = null
},
methods: {

init(editoroption) {

const editor = this.$refs.editor
if(editoroption!=null){
    this.options=editoroption;
    //this.Quill = new Quill(editor, this.options)
}else {

}
this.Quill = new Quill(editor, this.options);

this.Quill.pasteHTML(this.currentValue)
this.Quill.on('text-change', (delta, oldDelta, source) => {

const html = this.$refs.editor.children[0].innerHTML
const text = this.Quill.getText()
const quill = this.Quill
this.currentValue = html
this.$emit('input', html)
this.$emit('on-change', {
 html, text, quill })
})
this.Quill.on('text-change', (delta, oldDelta, source) => {

this.$emit('on-text-change', delta, oldDelta, source)
})
this.Quill.on('selection-change', (range, oldRange, source) => {

this.$emit('on-selection-change', range, oldRange, source)
})
this.Quill.on('editor-change', (eventName, ...args) => {

this.$emit('on-editor-change', eventName, ...args)
})
}
}
}
</script>
<style>
.editor, .ql-toolbar {

white-space: pre-wrap!important;
line-height: normal !important;
}
.quill-img {

display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {

content: "Vui lòng nhập địa chỉ liên kết:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {

border-right: 0;
content: "Lưu";
padding-right: 0;
}
.ql-snow .ql-tooltip[data-mode="video"]::before {

content: "Vui lòng nhập địa chỉ video:";
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {

content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {

content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {

content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {

content: "32px";
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {

content: "Văn bản";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {

content: "Tiêu đề1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {

content: "Tiêu đề2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {

content: "Tiêu đề3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {

content: "Tiêu đề4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {

content: "Tiêu đề5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {

content: "Tiêu đề6";
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {

content: "Phông chữ chuẩn";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {

content: "Phông chữ serif";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {

content: "Phông chữ đẳng rộng";
}
</style>
