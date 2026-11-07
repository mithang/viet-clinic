# Frontend Viet Clinic — Vue CLI 5 dev server (khớp cách chạy tay hiện tại:
# npm run dev, cổng 7020, proxy /api -> backend).
# Node 18: engines trong package.json yêu cầu >= 14; NODE_OPTIONS legacy
# provider giữ tương thích với webpack hashing cũ (giống máy dev).
# Lưu ý: CMD bắt buộc --host 0.0.0.0, nếu không dev server chỉ nghe
# localhost trong container và cổng publish 7020 sẽ không vào được.

FROM node:18-bookworm-slim
WORKDIR /app
COPY Code/medical2.0/client/package.json Code/medical2.0/client/package-lock.json ./
RUN npm install --no-audit --no-fund
COPY Code/medical2.0/client/ ./
EXPOSE 7020
ENV NODE_OPTIONS=--openssl-legacy-provider
CMD ["npm", "run", "dev", "--", "--port", "7020", "--host", "0.0.0.0"]
