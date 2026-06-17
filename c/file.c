#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void showMenu();

void createFile() {
  FILE *file = NULL;
  char fileName[50];
  char line[200];
  char choice;

  system("clear");
  printf("--- Quản lí file - Tạo file ---\n");
  printf("Nhập tên file: ");
  scanf("%s", fileName);
  getchar(); // xóa ký tự '\n' còn lại trong buffer

  file = fopen(fileName, "w");
  if(file) {
    printf("Nhập nội dung file (nhấn Enter 2 lần để kết thúc):\n");
    printf("--------------------------------------------------\n");

    while(1) {
      fgets(line, sizeof(line), stdin);
      // Nếu người dùng nhấn Enter trên dòng trống → kết thúc
      if(strcmp(line, "\n") == 0) break;
      fputs(line, file);
    }

    fclose(file);
    printf("--------------------------------------------------\n");
    printf("Tạo và ghi nội dung vào file '%s' thành công.\n", fileName);
  }
  else {
    printf("Không thể tạo file '%s'.\n", fileName);
  }

  showMenu();
}

void readFile() {
  FILE *file = NULL;
  char fileName[50];

  system("clear");
  printf("--- Quản lí file - Đọc file ---\n");
  printf("Nhập tên file: ");
  scanf("%s", fileName);
  file = fopen(fileName, "r");
  if(file) {
    printf("--------------------------------------------------\n");
    char fileContent[200];
    while(fgets(fileContent, sizeof(fileContent), file))
      printf("%s", fileContent);
    printf("\n--------------------------------------------------\n");
    fclose(file);
  }
  else printf("Không thể đọc file '%s'.\n", fileName);

  showMenu();
}

void deleteFile() {
  FILE *file = NULL;
  char fileName[50];

  system("clear");
  printf("--- Quản lí file - Xoá file ---\n");
  printf("Nhập tên file: ");
  scanf("%s", fileName);
  file = fopen(fileName, "r");
  if(file) {
    fclose(file);
    if (remove(fileName) == 0)
      printf("Xoá file '%s' thành công.\n", fileName);
    else
      printf("Không thể xoá file '%s'.\n", fileName);
  }
  else printf("File '%s' không tồn tại.\n", fileName);

  showMenu();
}

void showMenu() {
  int option = -1;

  printf("\n\n\n");
  printf("--- Quản lí file ---\n");
  printf("1. Tạo file\n");
  printf("2. Đọc file\n");
  printf("3. Xoá file\n");
  printf("0. Thoát\n");

  while(option < 0 || option > 3) {
    printf("Lựa chọn: ");
    scanf("%d", &option);
  }

  switch (option) {
    case 1: createFile(); break;
    case 2: readFile();   break;
    case 3: deleteFile(); break;
    case 0: exit(0);
  }
}

int main() {
  showMenu();
  return 0;
}
