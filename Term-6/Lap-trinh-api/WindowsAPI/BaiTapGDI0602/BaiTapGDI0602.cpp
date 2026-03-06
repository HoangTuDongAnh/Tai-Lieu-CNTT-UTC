// BaiTapGDI0602.cpp : Defines the entry point for the application.
//

#include "framework.h"
#include "BaiTapGDI0602.h"
#include "resource.h"

#define MAX_LOADSTRING 100

// Global Variables:
HINSTANCE hInst;                                // current instance
WCHAR szTitle[MAX_LOADSTRING];                  // The title bar text
WCHAR szWindowClass[MAX_LOADSTRING];            // the main window class name

COLORREF gStrokeColor = RGB(0, 0, 0);   // màu vẽ (nét ellipse)
int      gStrokeStyle = PS_SOLID;     // PS_SOLID / PS_DASH / PS_DOT
int      gStrokeWidth = 3;

COLORREF gBgColor = RGB(255, 255, 255); // màu nền
int      gBgStyle = 0;            // 0=solid, 1=hatch
int      gHatchStyle = HS_HORIZONTAL; // HS_HORIZONTAL / HS_VERTICAL / HS_FDIAGONAL ...


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
    LoadStringW(hInstance, IDC_BAITAPGDI0602, szWindowClass, MAX_LOADSTRING);
    MyRegisterClass(hInstance);

    // Perform application initialization:
    if (!InitInstance (hInstance, nCmdShow))
    {
        return FALSE;
    }

    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_BAITAPGDI0602));

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
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_BAITAPGDI0602));
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
    case WM_COMMAND:
        {
            int wmId = LOWORD(wParam);
            // Parse the menu selections:
            switch (wmId)
            {
			case ID_DR_BLACK: gStrokeColor = RGB(0, 0, 0); break;
			case ID_DR_BLUE: gStrokeColor = RGB(0, 0, 255); break;
			case ID_DR_GREEN: gStrokeColor = RGB(0, 255, 0); break;
			case ID_DR_RED: gStrokeColor = RGB(255, 0, 0); break;

            case ID_DRTY_DASH:
                gStrokeStyle = PS_DASH;
                gStrokeWidth = 1;   
                break;

            case ID_DRTY_DOT:
                gStrokeStyle = PS_DOT;
                gStrokeWidth = 1; 
                break;

            case ID_DRTY_SOLID:
                gStrokeStyle = PS_SOLID;
                gStrokeWidth = 3;
                break;

            case ID_BG_YELLOW:
                gBgColor = RGB(255, 255, 0);
				break;
            case ID_BG_VIOLET:
                gBgColor = RGB(238, 130, 238);
                break;
            case ID_BG_CYAN:
				gBgColor = RGB(0, 255, 255);
				break;
                
            case ID_BGTY_HOR:
                gBgStyle = 1;
                gHatchStyle = HS_HORIZONTAL;
				break;
            case ID_BGTY_VER:  
                gBgStyle = 1;
				gHatchStyle = HS_VERTICAL;
                break;
            case ID_BGTY_DIAG:
				gBgStyle = 1;
                gHatchStyle = HS_DIAGCROSS;
				break;
            case ID_BGTY_SOL:
                gBgStyle = 0;
				break;
            default:
                return DefWindowProc(hWnd, message, wParam, lParam);
            }
            InvalidateRect(hWnd, NULL, TRUE);
        }
        break;
    case WM_PAINT:
        {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hWnd, &ps);
            
            RECT rc;
			GetClientRect(hWnd, &rc);

            HBRUSH hBg = NULL;
            if (gBgStyle == 0) 
            {
                hBg = CreateSolidBrush(gBgColor);
            }
            else 
            {
                hBg = CreateHatchBrush(gHatchStyle, gBgColor);
			}

			FillRect(hdc, &rc, hBg);
			DeleteObject(hBg);

			HPEN hPen = CreatePen(gStrokeStyle, gStrokeWidth, gStrokeColor);
			HPEN oldPen = (HPEN)SelectObject(hdc, hPen);

			HBRUSH oldBrush = (HBRUSH)SelectObject(hdc, GetStockObject(NULL_BRUSH));

            int pad = 60;

			Ellipse(hdc, rc.left + pad, rc.top + pad, rc.right - pad, rc.bottom - pad);

			SelectObject(hdc, oldPen);
			SelectObject(hdc, oldBrush);
			DeleteObject(hPen);

            EndPaint(hWnd, &ps);
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
