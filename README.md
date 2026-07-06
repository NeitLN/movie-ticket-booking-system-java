## Chạy trong VS Code

Mở nguyên folder này trong VS Code, rồi chạy:

```bat
.\run.bat
```

Hoặc chạy thủ công:

```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -Dfile.encoding=UTF-8 -cp out com.bittersweetcinemas.ui.Main
```

## File chính

- `Main.java`: chạy app
- `HomeFrame.java`: layout trang chủ
- `MovieCard.java`: card phim
- `PosterPlaceholder.java`: khung poster trống
- `Theme.java`: màu sắc/font
- `RoundedButton.java`, `RoundedPanel.java`: component trang trí
