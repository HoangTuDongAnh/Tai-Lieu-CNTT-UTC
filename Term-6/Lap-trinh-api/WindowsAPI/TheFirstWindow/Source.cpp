#include <Windows.h>

// B9.1: Khai báo thủ tục cửa sổ
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam);

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nShowCmd) {
	
	// B1: Định nghĩa cấu trúc lớp cửa sổ
	WNDCLASS wndClass;
	static TCHAR appName[] = TEXT("VD1");

	// B2: Kiểu cách cửa sổ
	wndClass.style = CS_HREDRAW | CS_VREDRAW;

	// B3: Hàm xử lý thông điệp cửa sổ 
	wndClass.lpfnWndProc = WndProc; // Gọi là thủ tục cửa sổ, nơi xử lý các thông điệp đến cửa sổ

	wndClass.cbClsExtra = 0; // Số byte bổ sung cho lớp cửa sổ
	wndClass.cbWndExtra = 0; // Số byte bổ sung cho mỗi thể hiện cửa sổ

	wndClass.hInstance = hInstance; // Xử lý thể hiện của ứng dụng

	wndClass.hIcon = LoadIcon(hInstance, appName); // Biểu tượng cửa sổ

	wndClass.hCursor = LoadCursor(NULL, IDC_CROSS); // Con trỏ chuột

	wndClass.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH); // Màu nền cửa sổ

	wndClass.lpszMenuName = NULL;

	wndClass.lpszClassName = appName;

	// B4: Đăng ký lớp cửa sổ
	if (!RegisterClass(&wndClass)) {
		MessageBox(NULL, TEXT("Đăng ký cửa sổ lỗi"), TEXT("Error"), MB_OK | MB_ICONWARNING);
		return 0;
	}

	// B5: Tạo cửa sổ
	HWND hWnd = CreateWindow(
		appName,                    // Tên lớp cửa sổ
		TEXT("Ví dụ 1: Tạo cửa sổ cơ bản"), // Tiêu đề cửa sổ
		WS_OVERLAPPEDWINDOW,        // Kiểu cửa sổ
		CW_USEDEFAULT,              // Vị trí X của cửa sổ, đang set mặc định, nếu gán một giá trị thì cửa sổ sẽ xuất hiện tại vị trí đó
		CW_USEDEFAULT,              // Vị trí Y của cửa sổ
		CW_USEDEFAULT,              // Chiều rộng
		CW_USEDEFAULT,              // Chiều cao
		NULL,                       // Cửa sổ cha (Null là không có cha)
		NULL,                       // Menu (Null là không có menu)
		hInstance,                  // Xử lý thể hiện ứng dụng
		NULL                        // Tham số bổ sung
	);

	// B6: Hiển thị cửa sổ
	ShowWindow(hWnd, nShowCmd);

	// B7: Cập nhật cửa sổ
	UpdateWindow(hWnd);

	// B8: Vòng lặp thông điệp
	MSG msg;

	while (GetMessage(&msg, NULL, 0, 0)) { // Địa chỉ của biến msg, con trỏ NULL (lấy thông điệp từ tất cả cửa sổ của ứng dụng), 0, 0 (lấy tất cả các thông điệp)
		TranslateMessage(&msg); // Chuyển thông điệp từ số sang ký tự tương ứng
		DispatchMessage(&msg); // Chuyển thông điệp thủ tục sang cửa sổ tương ứng
	}

	return 1;
}

// B9.2: Định nghĩa thủ tục cửa sổ

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
	switch (msg) {
		PostQuitMessage(0);
		break;
	default:
		break;
	}
	return DefWindowProc(hwnd, msg, wParam, lParam);
}