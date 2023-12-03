# Autofocus and Auto White Balancing algorithm for Android Device
# Thuật toán lấy nét và cân bằng trắng tự động cho thiết bị Android

## 1. Giới thiệu
- Đây là một phần mềm được viết bằng ngôn ngữ Java, sử dụng Android Studio để phát triển, với mục đích thực hiện thuật toán lấy nét và cân bằng trắng tự động cho thiết bị Android.
- Phần mềm trong phạm vi môn học Kỹ Thuật Xử Lý & Truyền Thông Đa Phương Tiện, Trường Đại Học Công Nghệ Đại Học Quốc Gia Hà Nội.
- Phần mềm sử dụng Camera2 API và thư viện OpenCV. 
- Thành viên phát triển:
  - Nguyễn Khải Hoàn (trưởng nhóm)
  - Nguyễn Minh Hiếu
  - Nguyễn Quang Hoàn
  - Đinh Quốc Hiếu
  - Khúc Khải Hoàn
## 2. Các chức năng chính
- Chụp ảnh
- Lấy nét tự động (toàn màn hình)
- Lấy nét tự động (tự động phát hiện khuôn mặt)
- Lấy nét một vùng cụ thể
- Cân bằng trắng tự động
  - Gray-world algorithm
  - White patch reference
- Các chế độ cân bằng trắng theo nhiệt độ K
  - Daylight
  - Shade
  - Tungsten 
## 3. Screenshots
 - Ảnh chưa được lấy nét
  ![defocus.jpg](Images%2Fdefocus.jpg)
 - Ảnh đã được lấy nét
  ![focus.jpg](Images%2Ffocus.jpg)
 - Ảnh lấy nét gần
  ![focus_near.jpg](Images%2Ffocus_near.jpg)
 - Ảnh lấy nét xa
  ![focus_far.jpg](Images%2Ffocus_far.jpg)
 - Ảnh lấy nét khuôn mặt
  ![focus_face.jpg](Images%2Fface_1.jpg)
  ![focus_face.jpg](Images%2Fface_2.jpg)
 - Ảnh cân bằng trắng
  ![white_balance.jpg](Images%2Fwhite_balance.jpg)
 - Ảnh cân bằng trắng (Gray-world algorithm)
  ![white_balance_gray_world.jpg](Images%2Fwhite_balance_gray_world.jpg)
 - Ảnh cân bằng trắng (White patch reference)
  ![white_balance_white_patch.jpg](Images%2Fwhite_balance_white_patch.jpg)
 - Ảnh cân bằng trắng theo nhiệt độ K
   - Daylight
     ![daylight.jpg](Images%2Fdaylight.jpg)
   - Shade
     ![shade.jpg](Images%2Fdaylight.jpg)
   - Tungsten
   - ![tungsten.jpg](Images%2Ftungsten.jpg)

## 4. Video demo
- Auto Focus
  [AF1.mp4](Videos%2FAF1.mp4)
- Face Detection
  [AF2.mp4](Videos%2FAF2.mp4)
- Touch to Focus
  [AF3.mp4](Videos%2FAF3.mp4)
- White Balance
  [WB.mp4](Videos%2FWB.mp4)
