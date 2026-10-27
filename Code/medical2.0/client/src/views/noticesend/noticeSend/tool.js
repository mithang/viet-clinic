// toolbarTiêu đề
export const titleConfig = [
  { Choice: '.ql-insertMetric', title: '跳转配置' },
  { Choice: '.ql-bold', title: 'In đậm' },
  { Choice: '.ql-italic', title: 'Chữ nghiêng' },
  { Choice: '.ql-underline', title: 'Gạch chân' },
  { Choice: '.ql-header', title: '段落格式' },
  { Choice: '.ql-strike', title: 'Gạch ngang chữ' },
  { Choice: '.ql-blockquote', title: '块引用' },
  { Choice: '.ql-code', title: '插入代码' },
  { Choice: '.ql-code-block', title: '插入代码段' },
  { Choice: '.ql-font', title: '字体' },
  { Choice: '.ql-size', title: 'Cỡ chữ' },
  { Choice: '.ql-list[value="ordered"]', title: '编号列表' },
  { Choice: '.ql-list[value="bullet"]', title: '项目列表' },
  { Choice: '.ql-direction', title: 'Hướng văn bản' },
  { Choice: '.ql-header[value="1"]', title: 'h1' },
  { Choice: '.ql-header[value="2"]', title: 'h2' },
  { Choice: '.ql-align', title: 'Cách căn chỉnh' },
  { Choice: '.ql-color', title: 'Màu chữ' },
  { Choice: '.ql-background', title: 'Màu nền' },
  { Choice: '.ql-image', title: '图像' },
  { Choice: '.ql-video', title: 'Video' },
  { Choice: '.ql-link', title: '添加链接' },
  { Choice: '.ql-formula', title: '插入公式' },
  { Choice: '.ql-clean', title: '清除字体格式' },
  { Choice: '.ql-script[value="sub"]', title: 'Chỉ số dưới' },
  { Choice: '.ql-script[value="super"]', title: 'Chỉ số trên' },
  { Choice: '.ql-indent[value="-1"]', title: '向左缩进' },
  { Choice: '.ql-indent[value="+1"]', title: '向右缩进' },
  { Choice: '.ql-header .ql-picker-label', title: '标题大小' },
  { Choice: '.ql-header .ql-picker-item[data-value="1"]', title: '标题一' },
  { Choice: '.ql-header .ql-picker-item[data-value="2"]', title: '标题二' },
  { Choice: '.ql-header .ql-picker-item[data-value="3"]', title: '标题三' },
  { Choice: '.ql-header .ql-picker-item[data-value="4"]', title: '标题四' },
  { Choice: '.ql-header .ql-picker-item[data-value="5"]', title: '标题五' },
  { Choice: '.ql-header .ql-picker-item[data-value="6"]', title: '标题六' },
  { Choice: '.ql-header .ql-picker-item:last-child', title: 'Tiêu chuẩn' },
  { Choice: '.ql-size .ql-picker-item[data-value="small"]', title: '小号' },
  { Choice: '.ql-size .ql-picker-item[data-value="large"]', title: '大号' },
  { Choice: '.ql-size .ql-picker-item[data-value="huge"]', title: '超大号' },
  { Choice: '.ql-size .ql-picker-item:nth-child(2)', title: 'Tiêu chuẩn' },
  { Choice: '.ql-align .ql-picker-item:first-child', title: '居左对齐' },
  { Choice: '.ql-align .ql-picker-item[data-value="center"]', title: '居中对齐' },
  { Choice: '.ql-align .ql-picker-item[data-value="right"]', title: '居右对齐' },
  { Choice: '.ql-align .ql-picker-item[data-value="justify"]', title: '两端对齐' }
]


// Cấu hình thanh công cụ
export const toolbarOptions = [
  ["bold", "italic", "underline", "strike"], // In đậm Chữ nghiêng Gạch chân Gạch ngang chữ
  ["blockquote", "code-block"], // Trích dẫn  Khối mã
  [{ header: 1 }, { header: 2 }], // 1、2 级标题
  [{ list: "ordered" }, { list: "bullet" }], // Có thứ tự、Danh sách không thứ tự
  [{ script: "sub" }, { script: "super" }], // Chỉ số trên/Chỉ số dưới
  [{ indent: "-1" }, { indent: "+1" }], // Thụt lề
  // [{'direction': 'rtl'}],                         // Hướng văn bản
  [{ size: ["small", false, "large", "huge"] }], // Cỡ chữ
  [{ header: [1, 2, 3, 4, 5, 6, false] }], // Tiêu đề
  [{ color: [] }, { background: [] }], // Màu chữ、Màu nền chữ
  [{ font: [] }], // 字体种类
  [{ align: [] }], // Cách căn chỉnh
  ['link','image'],
  ["clean"], // Xóa định dạng văn bản
];
