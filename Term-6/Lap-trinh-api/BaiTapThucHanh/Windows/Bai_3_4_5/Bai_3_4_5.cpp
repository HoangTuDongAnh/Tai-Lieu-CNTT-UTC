// Bai_3_4_5.cpp : Defines the entry point for the application.
//

#include "framework.h"
#include "Bai_3_4_5.h"
#include "resource.h"

#define MAX_LOADSTRING 100

// Global Variables:
HINSTANCE hInst;                                // current instance
WCHAR szTitle[MAX_LOADSTRING];                  // The title bar text
WCHAR szWindowClass[MAX_LOADSTRING];            // the main window class name

// Forward declarations of functions included in this code module:
ATOM                MyRegisterClass(HINSTANCE hInstance);
BOOL                InitInstance(HINSTANCE, int);
LRESULT CALLBACK    WndProc(HWND, UINT, WPARAM, LPARAM);
INT_PTR CALLBACK    About(HWND, UINT, WPARAM, LPARAM);

int APIENTRY wWinMain(_In_ HINSTANCE hInstance,
                     _In_opt_ HINSTANCE hPrevInstance,
                     _In_ LPWSTR    lpCmdLine,
                     _In_ int       nCmdShow)
{
    UNREFERENCED_PARAMETER(hPrevInstance);
    UNREFERENCED_PARAMETER(lpCmdLine);

    // TODO: Place code here.

    // Initialize global strings
    LoadStringW(hInstance, IDS_APP_TITLE, szTitle, MAX_LOADSTRING);
    LoadStringW(hInstance, IDC_BAI345, szWindowClass, MAX_LOADSTRING);
    MyRegisterClass(hInstance);

    // Perform application initialization:
    if (!InitInstance (hInstance, nCmdShow))
    {
        return FALSE;
    }

    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_BAI345));

    MSG msg;

    // Main message loop:
    while (GetMessage(&msg, nullptr, 0, 0))
    {
        if (!TranslateAccelerator(msg.hwnd, hAccelTable, &msg))
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }

    return (int) msg.wParam;
}



//
//  FUNCTION: MyRegisterClass()
//
//  PURPOSE: Registers the window class.
//
ATOM MyRegisterClass(HINSTANCE hInstance)
{
    WNDCLASSEXW wcex;

    wcex.cbSize = sizeof(WNDCLASSEX);

    wcex.style          = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc    = WndProc;
    wcex.cbClsExtra     = 0;
    wcex.cbWndExtra     = 0;
    wcex.hInstance      = hInstance;
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_ICON1));
    wcex.hCursor        = LoadCursor(hInstance, MAKEINTRESOURCE(IDC_POINTER));
    wcex.hbrBackground  = (HBRUSH)(COLOR_WINDOW+1);
    wcex.lpszMenuName   = MAKEINTRESOURCEW(IDR_MENU_A);
    wcex.lpszClassName  = szWindowClass;
    wcex.hIconSm        = LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_ICON1));

    return RegisterClassExW(&wcex);
}

//
//   FUNCTION: InitInstance(HINSTANCE, int)
//
//   PURPOSE: Saves instance handle and creates main window
//
//   COMMENTS:
//
//        In this function, we save the instance handle in a global variable and
//        create and display the main program window.
//
BOOL InitInstance(HINSTANCE hInstance, int nCmdShow)
{
   hInst = hInstance; // Store instance handle in our global variable

   HWND hWnd = CreateWindowW(szWindowClass, szTitle, WS_OVERLAPPEDWINDOW,
      CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, nullptr, nullptr, hInstance, nullptr);

   if (!hWnd)
   {
      return FALSE;
   }

   ShowWindow(hWnd, nCmdShow);
   UpdateWindow(hWnd);

   return TRUE;
}

//
//  FUNCTION: WndProc(HWND, UINT, WPARAM, LPARAM)
//
//  PURPOSE: Processes messages for the main window.
//
//  WM_COMMAND  - process the application menu
//  WM_PAINT    - Paint the main window
//  WM_DESTROY  - post a quit message and return
//
//
LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    switch (message)
    {
    case WM_CONTEXTMENU:
            {
                // 1. Lấy tọa độ và chuẩn bị kiểm tra
                int xPos = LOWORD(lParam);
                int yPos = HIWORD(lParam);
                POINT ptMouse = { xPos, yPos };

                POINT ptClient = ptMouse;
                ScreenToClient(hWnd, &ptClient);

                RECT rcClient;
                GetClientRect(hWnd, &rcClient);

                HMENU hMenuToLoad = NULL;

                // 2. KIỂM TRA VỊ TRÍ ĐỂ CHỌN MENU CẦN LOAD
                if (PtInRect(&rcClient, ptClient))
                {
                    // A. Nếu chuột nằm trong vùng nội dung màu trắng
                    // -> Load menu Edit "chung"
					hMenuToLoad = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU_PU));
                }
                else
                {
                    // B. Nếu chuột nằm ở thanh tiêu đề hoặc viền
                    // -> Load menu chọn Ngôn ngữ
					hMenuToLoad = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU_NN));
                }

                // 3. HIỂN THỊ MENU (Code chung cho cả 2 trường hợp)
                if (hMenuToLoad)
                {
                    // Lấy menu con đầu tiên (Dummy) để hiển thị
                    HMENU hSubMenu = GetSubMenu(hMenuToLoad, 0);

                    if (hSubMenu)
                    {
                        TrackPopupMenu(hSubMenu, TPM_RIGHTBUTTON, xPos, yPos, 0, hWnd, NULL);
                    }

                    // Giải phóng bộ nhớ
                    DestroyMenu(hMenuToLoad);
                }

                // Nếu click vào thanh tiêu đề, ta return 0 để chặn menu hệ thống mặc định
                if (!PtInRect(&rcClient, ptClient)) return 0;
            }
        break;
    case WM_COMMAND:
        {
            int wmId = LOWORD(wParam);

            const wchar_t* msg = nullptr;

            switch (wmId)
            {
            case ID_CHON_A:
                {
                    HMENU hNewMenu = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU_A));
                    HMENU hOldMenu = GetMenu(hWnd);
                    if (SetMenu(hWnd, hNewMenu))
                    {
                        if (hOldMenu) DestroyMenu(hOldMenu);

                        DrawMenuBar(hWnd);
                    }
                }
                break;
            case ID_CHON_B:
                {
                    HMENU hNewMenu = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU_B));
                    HMENU hOldMenu = GetMenu(hWnd);
                    if (SetMenu(hWnd, hNewMenu))
                    {
                        if (hOldMenu) DestroyMenu(hOldMenu);

                        DrawMenuBar(hWnd);
                    }
                } 
                break;
            case ID_EDIT_UNDO:
            {
				msg = L"Bạn chọn lệnh Undo";
            }
            break;
            case ID_EDIT_REDO:
			{
				msg = L"Bạn chọn lệnh Redo";
			}
			break;
			case ID_EDIT_CUT:
			{
				msg = L"Bạn chọn lệnh Cut";
			}
			break;
			case ID_EDIT_COPY:
            {
				msg = L"Bạn chọn lệnh Copy";
            }
			break;
            case ID_EDIT_PASTE:
			{
				msg = L"Bạn chọn lệnh Paste";
			}
			break;
			case ID_EDIT_DELETE:
			{
				msg = L"Bạn chọn lệnh Delete";
			}
            default:
                return DefWindowProc(hWnd, message, wParam, lParam);
            }

            if (msg != nullptr)
            {
                MessageBox(hWnd, msg, L"Thông báo", MB_OK | MB_ICONINFORMATION);
            }
        }
        break;
    case WM_PAINT:
        {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hWnd, &ps);
            // TODO: Add any drawing code that uses hdc here...
            EndPaint(hWnd, &ps);
        }
        break;
    case WM_LBUTTONDOWN:
    {
        // Lấy tọa độ
        int x = LOWORD(lParam);
        int y = HIWORD(lParam);

        // Tạo bộ đệm để chứa chuỗi thông báo
        wchar_t buffer[256];

        // Định dạng chuỗi (Giống printf nhưng in vào biến buffer)
        wsprintf(buffer, L"Bạn vừa nhấn trái chuột, tọa độ: %d, %d", x, y);

        // Hiển thị thông báo
        MessageBox(hWnd, buffer, L"Thông báo", MB_OK | MB_ICONINFORMATION);
    }
    break;

    // 2. XỬ LÝ CLICK CHUỘT PHẢI
    case WM_RBUTTONDOWN:
    {
        int x = LOWORD(lParam);
        int y = HIWORD(lParam);

        wchar_t buffer[256];
        wsprintf(buffer, L"Bạn vừa nhấn phải chuột, tọa độ: %d, %d", x, y);

        MessageBox(hWnd, buffer, L"Thông báo", MB_OK | MB_ICONINFORMATION);

        // LƯU Ý QUAN TRỌNG: 
        // Nếu bạn có cả WM_CONTEXTMENU, hộp thoại này sẽ hiện lên TRƯỚC,
        // sau khi bấm OK thì Menu mới hiện ra.
    }
    break;
    case WM_CLOSE:
    {
        int result = MessageBox(hWnd, L"Bạn có muốn thoát khỏi chương trình không?", L"Xác nhận", MB_YESNO | MB_ICONQUESTION);

        if (result == IDYES)
        {
            // Nếu đồng ý thoát thì gọi hàm hủy cửa sổ
            DestroyWindow(hWnd);
        }
        else
        {
            // Nếu chọn No, ta return 0 để chặn việc đóng cửa sổ lại
            return 0;
        }
    }
    break;
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

// Message handler for about box.
INT_PTR CALLBACK About(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
    UNREFERENCED_PARAMETER(lParam);
    switch (message)
    {
    case WM_INITDIALOG:
        return (INT_PTR)TRUE;

    case WM_COMMAND:
        if (LOWORD(wParam) == IDOK || LOWORD(wParam) == IDCANCEL)
        {
            EndDialog(hDlg, LOWORD(wParam));
            return (INT_PTR)TRUE;
        }
        break;
    }
    return (INT_PTR)FALSE;
}
