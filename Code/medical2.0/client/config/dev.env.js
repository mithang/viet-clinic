'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  // BASE_API: '"http://10.1.1.116:9999/api"'
  BASE_API: '"/api"',	//Khi bật proxy table chỉ cần chỉ định đường dẫn truy cập



  // Cấu hình địa chỉ báo cáo
  // REPORT_CLIENT_URL: '"http://localhost:9528/"',
  // REPORT_SERVER_URL: '"http://localhost:8081/"',
  REPORT_CLIENT_URL: '"http://59.80.34.149:8099/dataease-dev/frontend/"',
  REPORT_SERVER_URL: '"http://59.80.34.149:8099/dataease-dev/backend/"',

  // Địa chỉ xem trước tệp trực tuyến
  // FILE_PREVIEW_URL: '"http://377328t1x9.zicp.vip:52265/"',
  // Địa chỉ xem trước trực tuyến kkFile
  KK_FILE_URL: '"http://192.168.0.31:8099/ffview/onlinePreview?url="',
  // Địa chỉ xem trước báo cáo UReport
  UREPORT_URL: '"http://localhost:9999/ureport/preview"',
  // Địa chỉ hội chẩn từ xa / họp trực tuyến
  VIDEO_CONF_URL: '"https://61.172.179.30:17015/"',
  // Địa chỉ dịch vụ theo dõi sau khám
  FOLLOW_UP_URL: '"http://61.172.179.73:5019"',
  // Địa chỉ bảng điều khiển màn hình lớn
  GRAFANA_URL: '"http://61.172.179.48:19017"',
})

