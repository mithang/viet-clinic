// toolbarTiêu đề
export const titleConfig = [
  { Choice: '.ql-insertMetric', title: 'Cấu hình chuyển hướng' },
  { Choice: '.ql-bold', title: 'In đậm' },
  { Choice: '.ql-italic', title: 'Chữ nghiêng' },
  { Choice: '.ql-underline', title: 'Gạch chân' },
  { Choice: '.ql-header', title: 'Định dạng đoạn văn' },
  { Choice: '.ql-strike', title: 'Gạch ngang chữ' },
  { Choice: '.ql-blockquote', title: 'Trích dẫn khối' },
  { Choice: '.ql-code', title: 'Chèn mã' },
  { Choice: '.ql-code-block', title: 'Chèn đoạn mã' },
  { Choice: '.ql-font', title: 'Phông chữ' },
  { Choice: '.ql-size', title: 'Cỡ chữ' },
  { Choice: '.ql-list[value="ordered"]', title: 'Danh sách đánh số' },
  { Choice: '.ql-list[value="bullet"]', title: 'Danh sách hạng mục' },
  { Choice: '.ql-direction', title: 'Hướng văn bản' },
  { Choice: '.ql-header[value="1"]', title: 'h1' },
  { Choice: '.ql-header[value="2"]', title: 'h2' },
  { Choice: '.ql-align', title: 'Cách căn chỉnh' },
  { Choice: '.ql-color', title: 'Màu chữ' },
  { Choice: '.ql-background', title: 'Màu nền' },
  { Choice: '.ql-image', title: 'Hình ảnh' },
  { Choice: '.ql-video', title: 'Video' },
  { Choice: '.ql-link', title: 'Thêm liên kết' },
  { Choice: '.ql-formula', title: 'Chèn công thức' },
  { Choice: '.ql-clean', title: 'Xóa định dạng phông chữ' },
  { Choice: '.ql-script[value="sub"]', title: 'Chỉ số dưới' },
  { Choice: '.ql-script[value="super"]', title: 'Chỉ số trên' },
  { Choice: '.ql-indent[value="-1"]', title: 'Thụt lề trái' },
  { Choice: '.ql-indent[value="+1"]', title: 'Thụt lề phải' },
  { Choice: '.ql-header .ql-picker-label', title: 'Cỡ tiêu đề' },
  { Choice: '.ql-header .ql-picker-item[data-value="1"]', title: 'Tiêu đề 1' },
  { Choice: '.ql-header .ql-picker-item[data-value="2"]', title: 'Tiêu đề 2' },
  { Choice: '.ql-header .ql-picker-item[data-value="3"]', title: 'Tiêu đề 3' },
  { Choice: '.ql-header .ql-picker-item[data-value="4"]', title: 'Tiêu đề 4' },
  { Choice: '.ql-header .ql-picker-item[data-value="5"]', title: 'Tiêu đề 5' },
  { Choice: '.ql-header .ql-picker-item[data-value="6"]', title: 'Tiêu đề 6' },
  { Choice: '.ql-header .ql-picker-item:last-child', title: 'Tiêu chuẩn' },
  { Choice: '.ql-size .ql-picker-item[data-value="small"]', title: 'Cỡ nhỏ' },
  { Choice: '.ql-size .ql-picker-item[data-value="large"]', title: 'Cỡ lớn' },
  { Choice: '.ql-size .ql-picker-item[data-value="huge"]', title: 'Cỡ rất lớn' },
  { Choice: '.ql-size .ql-picker-item:nth-child(2)', title: 'Tiêu chuẩn' },
  { Choice: '.ql-align .ql-picker-item:first-child', title: 'Căn trái' },
  { Choice: '.ql-align .ql-picker-item[data-value="center"]', title: 'Căn giữa' },
  { Choice: '.ql-align .ql-picker-item[data-value="right"]', title: 'Căn phải' },
  { Choice: '.ql-align .ql-picker-item[data-value="justify"]', title: 'Căn đều hai bên' }
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
