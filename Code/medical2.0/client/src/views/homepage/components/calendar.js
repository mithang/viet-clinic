/**
 * Danh sách tháng
 */
const monthList = [
  '1',
  '2',
  '3',
  '4',
  '5',
  '6',
  '7',
  '8',
  '9',
  '10',
  '11',
  '12'
]

/**
 * Thời gian hiện tại, năm, tháng, ngày
 * @param {* Thời gian khởi tạo } date
 */
const getNewDate = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth()
  const day = date.getDate()
  return { year, month, day }
}

const getDate = (year, month, day) => {
  return new Date(year, month, day)
}

const englishMonth = (month) => {
  let engMonth

  monthList.map(() => {
    engMonth = monthList[month]
  })

  return engMonth
}

const formatDate = (date) => {
  date = Number(date)
  return date < 10 ? `0${date}` : date
}

export {
  getNewDate,
  getDate,
  englishMonth,
  formatDate
}
