#!/bin/sh

handleNano() {
  status=$?
  if [ $status -eq 0 ] || [ $status -eq 1 ]; then
    echo "Lưu file '$1' thành công."
  elif [ $status -eq 2 ]; then
    echo "Không thể mở file '$1'."
  else
    echo "Lỗi không xác định ($status)."
  fi
}

waitEnter() {
  echo "Nhấn Enter để quay lại menu..."
  read -r line < /dev/tty
}

createFile() {
  clear
  echo "--- Quản lí file - Tạo file ---"
  read -p "Nhập tên file: " fileName < /dev/tty
  nano "$fileName" < /dev/tty
  handleNano "$fileName"
  waitEnter
  showMenu
}

readFile() {
  clear
  echo "--- Quản lí file - Đọc file ---"
  read -p "Nhập tên file: " fileName < /dev/tty
  if [ -f "$fileName" ]; then
    echo "----------------------------------"
    cat "$fileName"
    echo "----------------------------------"
  else
    echo "File '$fileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

updateFile() {
  clear
  echo "--- Quản lí file - Sửa file ---"
  read -p "Nhập tên file: " fileName < /dev/tty
  if [ -f "$fileName" ]; then
    nano "$fileName" < /dev/tty
    handleNano "$fileName"
  else
    echo "File '$fileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

renameFile() {
  clear
  echo "--- Quản lí file - Đổi tên file ---"
  read -p "Nhập tên file cũ: " oldFileName < /dev/tty
  if [ -f "$oldFileName" ]; then
    read -p "Nhập tên file mới: " newFileName < /dev/tty
    mv "$oldFileName" "$newFileName"
    echo "Đổi tên file từ '$oldFileName' thành '$newFileName' thành công."
  else
    echo "File '$oldFileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

copyFile() {
  clear
  echo "--- Quản lí file - Sao chép file ---"
  read -p "Nhập tên file nguồn: " sourceFileName < /dev/tty
  if [ -f "$sourceFileName" ]; then
    read -p "Nhập tên file đích: " destinationFileName < /dev/tty
    cp "$sourceFileName" "$destinationFileName"
    echo "Sao chép file từ '$sourceFileName' đến '$destinationFileName' thành công."
  else
    echo "File '$sourceFileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

compressFile() {
  clear
  echo "--- Quản lí file - Nén file ---"
  read -p "Nhập tên file muốn nén: " fileName < /dev/tty
  if [ -f "$fileName" ]; then
    read -p "Nhập tên file nén (bao gồm đuôi .tar.gz): " compressedFileName < /dev/tty
    tar -czvf "$compressedFileName" "$fileName"
    echo "Nén file '$fileName' thành công, lưu thành '$compressedFileName'."
  else
    echo "File '$fileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

deleteFile() {
  clear
  echo "--- Quản lí file - Xoá file ---"
  read -p "Nhập tên file: " fileName < /dev/tty
  if [ -f "$fileName" ]; then
    rm "$fileName"
    echo "Xoá file '$fileName' thành công."
  else
    echo "File '$fileName' không tồn tại."
  fi
  waitEnter
  showMenu
}

showMenu() {
  clear
  echo
  echo "--- Quản lí file ---"
  echo "1. Tạo file"
  echo "2. Đọc file"
  echo "3. Sửa file"
  echo "4. Đổi tên file"
  echo "5. Sao chép file"
  echo "6. Nén file"
  echo "7. Xóa file"
  echo "0. Thoát"
  read -p "Lựa chọn: " option < /dev/tty
  case $option in
    1) createFile ;;
    2) readFile ;;
    3) updateFile ;;
    4) renameFile ;;
    5) copyFile ;;
    6) compressFile ;;
    7) deleteFile ;;
    0) exit 0 ;;
    *)
      echo "Vui lòng nhập đúng lựa chọn (0-7)."
      read -p "Nhấn Enter để thử lại..." < /dev/tty
      showMenu
      ;;
  esac
}

showMenu
