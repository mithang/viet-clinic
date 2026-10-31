// import { createCellPos } from './translateNumToLetter'
const Excel = require('exceljs')

import FileSaver from 'file-saver'

export const exportExcel = (luckysheet, value, needFormula) => {
  // Tham số là đối tượng lấy từ luckysheet.getluckysheetfile()
  // 1.Tạo workbook, có thể thêm thuộc tính cho workbook
  const workbook = new Excel.Workbook()
  // 2.Tạo bảng, tham số thứ hai có thể cấu hình loại worksheet tạo ra
  if (Object.prototype.toString.call(luckysheet) === '[object Object]') {
    luckysheet = [luckysheet]
  }
  luckysheet.forEach((table) => {
    if (table.data.length === 0) return  true
    // ws.getCell('B2').fill = fills.
    const worksheet = workbook.addWorksheet(table.name)
    const merge = (table.config && table.config.merge) || {}
    const borderInfo = (table.config && table.config.borderInfo) || {}
    // 3.Thiết lập gộp ô, thiết lập viền ô, thiết lập kiểu ô, thiết lập giá trị
    setStyleAndValue(table.data, worksheet, needFormula)
    setMerge(merge, worksheet)
    setBorder(borderInfo, worksheet)
    return true
  })

  // return
  // 4.Ghi buffer
  const buffer = workbook.xlsx.writeBuffer().then(data => {
      // console.log('data', data)
      const blob = new Blob([data], {
        type: 'application/vnd.ms-excel;charset=utf-8'
      })
      FileSaver.saveAs(blob, `${value}.xlsx`)
})
  return buffer
}

const setMerge = (luckyMerge = {}, worksheet) => {
  const mergearr = Object.values(luckyMerge)
  mergearr.forEach((elem) => {
    // elem định dạng: {r: 0, c: 0, rs: 1, cs: 2}
    // Gộp theo hàng bắt đầu, cột bắt đầu, hàng kết thúc, cột kết thúc (tương đương K10:M12)
    worksheet.mergeCells(
      elem.r + 1,
      elem.c + 1,
      elem.r + elem.rs,
      elem.c + elem.cs
    )
  })
}

const setBorder = (luckyBorderInfo, worksheet) => {
  if (!Array.isArray(luckyBorderInfo)) return
  // console.log('luckyBorderInfo', luckyBorderInfo)
  luckyBorderInfo.forEach((elem) => {
    // Hiện tại chỉ tương thích khi borderType là range
    // console.log('ele', elem)
    if (elem.rangeType === 'range') {
      let border = borderConvert(elem.borderType, elem.style, elem.color)
      let rang = elem.range[0]
      // console.log('range', rang)
      let row = rang.row
      let column = rang.column
      for (let i = row[0] + 1; i < row[1] + 2; i++) {
        for (let y = column[0] + 1; y < column[1] + 2; y++) {
          worksheet.getCell(i, y).border = border
        }
      }
    }
    if (elem.rangeType === 'cell') {
      // col_index: 2
      // row_index: 1
      // b: {
      //   color: '#d0d4e3'
      //   style: 1
      // }
      const { col_index, row_index } = elem.value
      const borderData = Object.assign({}, elem.value)
      delete borderData.col_index
      delete borderData.row_index
      let border = addborderToCell(borderData, row_index, col_index)
      // console.log('bordre', border, borderData)
      worksheet.getCell(row_index + 1, col_index + 1).border = border
    }
    // console.log(rang.column_focus + 1, rang.row_focus + 1)
    // worksheet.getCell(rang.row_focus + 1, rang.column_focus + 1).border = border
  })
}
const setStyleAndValue = (cellArr, worksheet, needFormula) => {
  if (!Array.isArray(cellArr)) return
  cellArr.forEach((row, rowid) => {
    row.every((cell, columnid) => {
      if (!cell) return true
      let fill = fillConvert(cell.bg)

      let font = fontConvert(
        cell.ff,
        cell.fc,
        cell.bl,
        cell.it,
        cell.fs,
        cell.cl,
        cell.ul
      )
      let alignment = alignmentConvert(cell.vt, cell.ht, cell.tb, cell.tr)
      let value = ''

      if (cell.f && needFormula) {
        value = { formula: cell.f, result: cell.v }
      } else if (!cell.v && cell.ct && cell.ct.s) {
        // xls chuyển xong sang xlsx, bên trong tồn tại định dạng khác nhau, đều sẽ vào rich text, tức giá trị không tồn tại trong cell.v, mà tồn tại trong cell.ct.s
        // value = cell.ct.s[0].v
        cell.ct.s.forEach(arr => {
          value += arr.v
      })
      } else {
        value = cell.v
      }
      //  style điền vào _value có thể thực hiện màu nền ô
      let letter = createCellPos(columnid)
      let target = worksheet.getCell(letter + (rowid + 1))
      // console.log('1233', letter + (rowid + 1))
      for (const key in fill) {
        target.fill = fill
        break
      }
      target.font = font
      target.alignment = alignment
      target.value = value

      return true
    })
  })
}

const fillConvert = (bg) => {
  if (!bg) {
    return {}
  }
  // const bgc = bg.replace('#', '')
  let fill
  let color
  // console.log(bg)
  if (bg.indexOf('rgb') === -1) {
    color = bg.replace('#', '')
  } else {
    color = bg.colorHex().replace('#', '')
  }
  fill = {
    type: 'pattern',
    pattern: 'solid',
    fgColor: { argb: color}
  }

  return fill
}

const fontConvert = (
  ff = 0,
  fc = '#000000',
  bl = 0,
  it = 0,
  fs = 10,
  cl = 0,
  ul = 0
) => {
  // luckysheet: ff(Kiểu), fc(Màu), bl(Chữ đậm), it(Chữ nghiêng), fs(Kích thước), cl(Gạch ngang chữ), ul(Gạch chân)
  // Ánh xạ chỉ số font của luckysheet sang tên font hỗ trợ tiếng Việt.
  // Chỉ dùng khi ff là số (đọc từ file luckysheet); nếu ff là chuỗi thì giữ nguyên.
  const luckyToExcel = {
    0: 'Arial',
    1: 'Times New Roman',
    2: 'Calibri',
    3: 'Tahoma',
    4: 'Verdana',
    5: 'Segoe UI',
    6: 'Cambria',
    7: 'Georgia',
    8: 'Courier New',
    9: 'Arial',
    10: 'Times New Roman ',
    11: 'Tahoma ',
    12: 'Verdana',
    num2bl(num) {
      return num === 0 ? false : true
    }
  }
  // Xảy ra Bug, khi import thì ff là val của luckyToExcel
  let color
  if (fc.indexOf('rgb') === -1) {
    color = fc.replace('#', '')
  } else {
    color = fc.colorHex().replace('#', '')
  }

  let font = {
    name: typeof ff === 'number' ? luckyToExcel[ff] : ff,
    family: 1,
    size: fs,
    color: { argb: color },
    bold: luckyToExcel.num2bl(bl),
    italic: luckyToExcel.num2bl(it),
    underline: luckyToExcel.num2bl(ul),
    strike: luckyToExcel.num2bl(cl)
  }

  return font
}

const alignmentConvert = (
  vt = 'default',
  ht = 'default',
  tb = 'default',
  tr = 'default'
) => {
  //luckysheet:vt(Đứng), ht(Ngang), tb(Xuống dòng), tr(Xoay)
  const luckyToExcel = {
    vertical: {
      0: 'middle',
      1: 'top',
      2: 'bottom',
      default: 'top'
    },
    horizontal: {
      0: 'center',
      1: 'left',
      2: 'right',
      default: 'left'
    },
    wrapText: {
      0: false,
      1: false,
      2: true,
      default: false
    },
    textRotation: {
      0: 0,
      1: 45,
      2: -45,
      3: 'vertical',
      4: 90,
      5: -90,
      default: 0
    }
  }

  let alignment = {
    vertical: luckyToExcel.vertical[vt],
    horizontal: luckyToExcel.horizontal[ht],
    wrapText: luckyToExcel.wrapText[tb],
    textRotation: luckyToExcel.textRotation[tr]
  }
  return alignment
}

const borderConvert = (borderType, style = 1, color = '#000') => {
  // Tương ứng tham số borderinfo trong config của luckysheet
  if (!borderType) {
    return {}
  }
  const luckyToExcel = {
    type: {
      'border-all': 'all',
      'border-top': 'top',
      'border-right': 'right',
      'border-bottom': 'bottom',
      'border-left': 'left'
    },
    style: {
      0: 'none',
      1: 'thin',
      2: 'hair',
      3: 'dotted',
      4: 'dashDot', // 'Dashed',
      5: 'dashDot',
      6: 'dashDotDot',
      7: 'double',
      8: 'medium',
      9: 'mediumDashed',
      10: 'mediumDashDot',
      11: 'mediumDashDotDot',
      12: 'slantDashDot',
      13: 'thick'
    }
  }

  let color1
  if (color.indexOf('rgb') === -1) {
    color1 = color.replace('#', '')
  } else {
    color1 = color.colorHex().replace('#', '')
  }


  let template = {
    style: luckyToExcel.style[style],
    color: { argb: color1 }
  }
  let border = {}
  if (luckyToExcel.type[borderType] === 'all') {
    border['top'] = template
    border['right'] = template
    border['bottom'] = template
    border['left'] = template
  } else {
    border[luckyToExcel.type[borderType]] = template
  }
  // console.log('border', border)
  return border
}

function addborderToCell(borders, row_index, col_index) {
  let border = {}
  const luckyExcel = {
    type: {
      l: 'left',
      r: 'right',
      b: 'bottom',
      t: 'top'
    },
    style: {
      0: 'none',
      1: 'thin',
      2: 'hair',
      3: 'dotted',
      4: 'dashDot', // 'Dashed',
      5: 'dashDot',
      6: 'dashDotDot',
      7: 'double',
      8: 'medium',
      9: 'mediumDashed',
      10: 'mediumDashDot',
      11: 'mediumDashDotDot',
      12: 'slantDashDot',
      13: 'thick'
    }
  }
  // console.log('borders', borders)
  for (const bor in borders) {
    // console.log(borders)
    // console.log(borders[bor])
    if (borders[bor] != null) {
      if (borders[bor].color.indexOf('rgb') === -1) {
        border[luckyExcel.type[bor]] = {
          style: luckyExcel.style[borders[bor].style],
          color: { argb: borders[bor].color.replace('#', '') }
        }
      } else {
        border[luckyExcel.type[bor]] = {
          style: luckyExcel.style[borders[bor].style],
          color: { argb: borders[bor].color.colorHex().replace('#', '') }
        }
      }
    }
  }

  return border
}

function createCellPos(n) {
  let ordA = 'A'.charCodeAt(0)

  let ordZ = 'Z'.charCodeAt(0)
  let len = ordZ - ordA + 1
  let s = ''
  while (n >= 0) {
    s = String.fromCharCode((n % len) + ordA) + s

    n = Math.floor(n / len) - 1
  }
  return s
}


/**
 * Standalone function to convert an RGB color string to hex format.
 * @param {string} str - RGB color string (e.g. "rgb(255, 0, 0)") or already hex
 * @returns {string} Hex color string (e.g. "#ff0000")
 */
export function colorHex(str) {
  const reg = /^(rgb|RGB)/;
  if (reg.test(str)) {
    let strHex = "#";
    const colorArr = str.replace(/(?:\(|\)|rgb|RGB)*/g, "").split(",");
    for (let i = 0; i < colorArr.length; i++) {
      let hex = Number(colorArr[i]).toString(16);
      if (hex === "0") {
        hex += hex;
      }
      strHex += hex;
    }
    return strHex;
  } else {
    return String(str);
  }
}
