// thuchanh1.cpp : Defines the entry point for the application.
//

#include "framework.h"
#include "thuchanh1.h"
#include "resource.h"

#define MAX_LOADSTRING 100

// Global Variables:
HINSTANCE hInst;                                // current instance
WCHAR szTitle[MAX_LOADSTRING];                  // The title bar text
WCHAR szWindowClass[MAX_LOADSTRING];            // the main window class name
HMENU hmenuPopup;
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
    LoadStringW(hInstance, IDC_THUCHANH1, szWindowClass, MAX_LOADSTRING);
    MyRegisterClass(hInstance);

    // Perform application initialization:
    if (!InitInstance (hInstance, nCmdShow))
    {
        return FALSE;
    }

    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_THUCHANH1));

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
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_THUCHANH1));
    wcex.hCursor        = LoadCursor(nullptr, IDC_ARROW);
    wcex.hbrBackground  = (HBRUSH)(COLOR_WINDOW+1);
    wcex.lpszMenuName   = MAKEINTRESOURCEW(IDR_MENU1);
    wcex.lpszClassName  = szWindowClass;
    wcex.hIconSm        = LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_SMALL));

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
   hmenuPopup = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU2));
   HWND hWnd = CreateWindowW(szWindowClass, TEXT("ThanhHang"), WS_OVERLAPPEDWINDOW,
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
    static HDC hdc;
    static POINT pt[3],point;
    static int width, height, xLeft, yTop, xRight, yBottom, Hinh,m=0,s=0;
    static HPEN hpen = CreatePen(PS_SOLID, 3, RGB(0, 0, 0));
    static HBRUSH hBrush = (HBRUSH)GetStockObject(WHITE_BRUSH);
    static HMENU menuNen;
    static TCHAR leftTime[20];

    switch (message)
    {
    case WM_CREATE:
        SetTimer(hWnd, 1, 1000, NULL);
        break;
    case WM_SIZE:
        width = LOWORD(lParam);
        height = HIWORD(lParam);
        break;
    case WM_TIMER:
        if (s>=0 && s<59)
        {
            s = s + 1;
        }
        else {
            if (s == 59) {
                s = 0;
                m = m + 1;
            }
        }
        wsprintfW(leftTime, L"Time : %d:%d", m, s);
        hdc = GetDC(hWnd);
        TextOut(hdc, width - 120, height - 20, leftTime, 20);
        ReleaseDC(hWnd,hdc);
        break;
    case WM_RBUTTONDOWN:
        menuNen = GetSubMenu(hmenuPopup, 0);
        point.x = LOWORD(lParam);
        point.y = HIWORD(lParam);
        ClientToScreen(hWnd, &point);
        TrackPopupMenu(menuNen, TPM_RIGHTBUTTON, point.x, point.y, 
            0, hWnd, NULL);

        break;
    case WM_LBUTTONDOWN:
        xLeft = LOWORD(lParam);
        yTop = HIWORD(lParam);
        break;
    case WM_LBUTTONUP:
        xRight = LOWORD(lParam);
        yBottom = HIWORD(lParam);
        //ve hinh
        hdc = GetDC(hWnd);
        SelectObject(hdc, hpen);
        SelectObject(hdc, hBrush);
        if (Hinh == ID_TGV) {
            pt[0].x = xLeft;
            pt[0].y = yTop;
            pt[1].x = xRight;
            pt[1].y = yBottom;
            pt[2].x = xLeft;
            pt[2].y = yBottom;
            Polygon(hdc, pt, 3);
        }
        if (Hinh == ID_DT) {
            MoveToEx(hdc, xLeft, yTop, NULL);
            LineTo(hdc, xRight, yBottom);
        }
            ReleaseDC(hWnd, hdc);
        break;
    case WM_CLOSE:
        if (MessageBox(NULL, TEXT("Ban co muon thoat khong"), 
            TEXT("Yes or No"), MB_YESNO | MB_ICONQUESTION) == IDYES)
        {
            DestroyWindow(hWnd);
        }
        break;
    case WM_COMMAND:
        {
            int wmId = LOWORD(wParam);
            // Parse the menu selections:
            switch (wmId)
            {
            case ID_DO:
                hpen = CreatePen(PS_SOLID,3,RGB(255,0,0));
                break;
            case ID_XANH:
                hpen = CreatePen(PS_SOLID, 3, RGB(0,255,0));
                break;
            case ID_TGV:
            case ID_DT:
                Hinh = wmId;
                break;
            case IDM_ABOUT:
                DialogBox(hInst, MAKEINTRESOURCE(IDD_ABOUTBOX), hWnd, About);
                break;
            case IDM_EXIT:
                DestroyWindow(hWnd);
                break;
            default:
                return DefWindowProc(hWnd, message, wParam, lParam);
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
    case WM_DESTROY:
        PostQuitMessage(0);
        DeleteObject(hBrush);
        DeleteObject(hpen);
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
