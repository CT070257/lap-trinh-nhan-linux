#!/bin/sh

createCronJob() {
  clear
  echo "--- Lập lịch tác vụ - Tạo tác vụ ---"
  read -p "Nhập đường dẫn tới file shell: " filePath
  if [ -f "$filePath" ]; then
    read -r -p "Nhập cron expression: " cronJobExpression
    chmod +x "$filePath"
    crontab -l | { cat; echo "$cronJobExpression $filePath"; } | crontab -
    if [ $? -eq 0 ]; then
       echo "Tạo tác vụ '$filePath' thành công."
    else
      echo "Tạo tác vụ '$filePath' thất bại."
    fi
  else
    echo "File '$filePath' không tồn tại."
  fi
  showMenu
}

listCronJobs() {
  clear
  echo "--- Lập lịch tác vụ - Danh sách tác vụ ---"
  crontab -l
  showMenu
}

deleteCronJob() {
  clear
  echo "--- Lập lịch tác vụ - Xoá tác vụ ---"
  echo "Danh sách các tác vụ hiện tại:"
  crontab -l
  echo "------------------------------------"
  read -p "Nhập đường dẫn file shell của tác vụ cần xoá: " filePath
  
  # Kiểm tra xem có crontab nào chứa đường dẫn này không
  if crontab -l | grep -q "$filePath"; then
    # Đọc crontab, lọc bỏ (-v) dòng chứa filePath, rồi ghi đè lại vào crontab
    crontab -l | grep -v "$filePath" | crontab -
    echo "✅ Đã xoá tác vụ chứa '$filePath'."
  else
    echo "❌ Không tìm thấy tác vụ nào chứa '$filePath'."
  fi
  showMenu
}

showMenu() {
  echo
  echo
  echo "--- Lập lịch tác vụ ---"
  echo "1. Tạo tác vụ"
  echo "2. Xoá tác vụ"
  echo "3. Danh sách tác vụ"
  echo "0. Thoát"
  read -p "Lựa chọn: " option
  
  case $option in
    1)
      createCronJob
      ;;
    2)
      deleteCronJob
      ;;
    3)
      listCronJobs
      ;;
    0)
      exit 0
      ;;
    *)
      clear
      echo "Vui lòng nhập đúng lựa chọn"
      showMenu
      ;;
  esac
}

showMenu
