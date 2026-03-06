// GDIExample.cpp : Defines the entry point for the application.
//

#include "framework.h"
#include "GDIExample.h"

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
    LoadStringW(hInstance, IDC_GDIEXAMPLE, szWindowClass, MAX_LOADSTRING);
    MyRegisterClass(hInstance);

    // Perform application initialization:
    if (!InitInstance (hInstance, nCmdShow))
    {
        return FALSE;
    }

    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_GDIEXAMPLE));

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
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_GDIEXAMPLE));
    wcex.hCursor        = LoadCursor(nullptr, IDC_ARROW);
    wcex.hbrBackground  = (HBRUSH)(COLOR_WINDOW+1);
    wcex.lpszMenuName   = MAKEINTRESOURCEW(IDC_GDIEXAMPLE);
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
    static HDC hdc;
    static POINT pt;

    static POINT pStart, pEnd;
    static bool isDrawing = false;

    static HPEN redPen, violetPen, yellowPen;


    switch (message)
    {
    case WM_COMMAND:
        {
            int wmId = LOWORD(wParam);
            // Parse the menu selections:
            switch (wmId)
            {
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

  //  case WM_LBUTTONDOWN:
  //  {
		//pt.x = LOWORD(lParam);
		//pt.y = HIWORD(lParam);

  //      // lấy device context của cửa sổ
  //      HDC hdc = GetDC(hWnd);

  //      // vẽ 1 điểm ảnh tại vị trí click
  //      SetPixel(hdc, pt.x, pt.y, RGB(255, 0, 0));

  //      // giải phóng DC
  //      ReleaseDC(hWnd, hdc);
  //  }
  //  break;
    case WM_PAINT:
    {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hWnd, &ps);

        RECT rc; GetClientRect(hWnd, &rc);
        int w = rc.right - rc.left;
        int h = rc.bottom - rc.top;

        int S = (w < h) ? w : h;                 // kích thước chuẩn
        auto P = [&](int pct) { return (S * pct) / 100; }; // tiện tính %

        // ===== PEN/BRUSH =====
        HPEN pen = CreatePen(PS_SOLID, 1, RGB(0, 0, 0));
        HGDIOBJ oldPen = SelectObject(hdc, pen);
        SetBkMode(hdc, TRANSPARENT);

        HBRUSH brYellow = CreateSolidBrush(RGB(255, 220, 0));
        HBRUSH brWhite = CreateSolidBrush(RGB(255, 255, 255));

        SetBkColor(hdc, RGB(230, 255, 250));
        SetTextColor(hdc, RGB(0, 190, 170));
        HBRUSH brHatch = CreateHatchBrush(HS_DIAGCROSS, RGB(0, 190, 170));

        HGDIOBJ oldBrush = SelectObject(hdc, GetStockObject(NULL_BRUSH));

        // ===== 1) HCN vàng (khung trong) =====
        int mInner = P(7);          // ~7% của min(w,h) (bạn có thể 6..10)
        int iL = mInner, iT = mInner, iR = w - mInner, iB = h - mInner;

        SelectObject(hdc, brYellow);
        Rectangle(hdc, iL, iT, iR, iB);

        // ===== 2) Ellipse trắng =====
        int ePad = P(3);            // ~3% (tạo khoảng hở từ HCN -> ellipse)
        int eL = iL + ePad, eT = iT + ePad, eR = iR - ePad, eB = iB - ePad;

        SelectObject(hdc, brWhite);
        Ellipse(hdc, eL, eT, eR, eB);

        // ===== 3) Đường chéo từ 4 góc màn hình tới ellipse (cắt tại biên ellipse) =====
        {
            HRGN rAll = CreateRectRgn(0, 0, w, h);
            HRGN rElps = CreateEllipticRgn(eL, eT, eR, eB);
            CombineRgn(rAll, rAll, rElps, RGN_DIFF); // all - ellipse

            int saved = SaveDC(hdc);
            SelectClipRgn(hdc, rAll);

            int ex = (eL + eR) / 2;
            int ey = (eT + eB) / 2;

            MoveToEx(hdc, 0, 0, NULL); LineTo(hdc, ex, ey);
            MoveToEx(hdc, w, 0, NULL); LineTo(hdc, ex, ey);
            MoveToEx(hdc, 0, h, NULL); LineTo(hdc, ex, ey);
            MoveToEx(hdc, w, h, NULL); LineTo(hdc, ex, ey);

            RestoreDC(hdc, saved);
            DeleteObject(rElps);
            DeleteObject(rAll);
        }

        // ===== 4) RoundRect hatch xanh ngọc =====
        {
            // lấy theo ellipse cho luôn cân
            int rrW = (eR - eL) * 58 / 100;   // ~58% bề ngang ellipse
            int rrH = (eB - eT) * 38 / 100;   // ~38% bề cao ellipse

            int cx = (eL + eR) / 2;
            int cy = (eT + eB) / 2;

            int rrL = cx - rrW / 2, rrT = cy - rrH / 2;
            int rrR = cx + rrW / 2, rrB = cy + rrH / 2;

            int rx = rrW / 5;                // bo góc theo tỉ lệ width/height
            int ry = rrH / 2;

            SelectObject(hdc, brHatch);
            RoundRect(hdc, rrL, rrT, rrR, rrB, rx, ry);
        }

        // ===== CLEANUP =====
        SelectObject(hdc, oldBrush);
        SelectObject(hdc, oldPen);

        DeleteObject(brYellow);
        DeleteObject(brWhite);
        DeleteObject(brHatch);
        DeleteObject(pen);

        EndPaint(hWnd, &ps);
    }
    break;

    /*case WM_LBUTTONDOWN:
    {
        pStart.x = LOWORD(lParam);
        pStart.y = HIWORD(lParam);

        pEnd = pStart;
        isDrawing = true;
    }
    break;
    case WM_MOUSEMOVE:
    {
        if (isDrawing)
        {
            pEnd.x = LOWORD(lParam);
            pEnd.y = HIWORD(lParam);

            InvalidateRect(hWnd, NULL, TRUE);
        }
    }
    break;
    case WM_LBUTTONUP:
    {
        pEnd.x = LOWORD(lParam);
        pEnd.y = HIWORD(lParam);

        isDrawing = false;
        InvalidateRect(hWnd, NULL, TRUE);
    }
    break;
    case WM_PAINT:
    {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hWnd, &ps);

        MoveToEx(hdc, pStart.x, pStart.y, NULL);
        LineTo(hdc, pEnd.x, pEnd.y);

        EndPaint(hWnd, &ps);
    }
    break;*/
    case WM_DESTROY:
        DeleteObject(redPen);
		DeleteObject(violetPen);
		DeleteObject(yellowPen);
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
