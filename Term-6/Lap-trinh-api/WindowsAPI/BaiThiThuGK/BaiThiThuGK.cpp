// BaiThiThuGK.cpp : Defines the entry point for the application.
//

#include "framework.h"
#include "BaiThiThuGK.h"
#include "resource.h"
#include <math.h>

#define MAX_LOADSTRING 100

// Global Variables:
HINSTANCE hInst;
WCHAR szTitle[MAX_LOADSTRING];
WCHAR szWindowClass[MAX_LOADSTRING];

POINT startPt, endPt;
bool isDragging = false;
bool hasShape = false;

// kieu hinh
enum ShapeType
{
    SHAPE_ARROW,
    SHAPE_RECT,
    SHAPE_TRIANGLE
};

ShapeType currentShape = SHAPE_ARROW;

// bien luu hien tai
COLORREF currentFillColor = RGB(0, 0, 255);
int currentHatch = HS_VERTICAL;
UINT_PTR timerID = 1;
int remainingSeconds = 30 * 60;

COLORREF shapeFillColor = RGB(0, 0, 255);
int shapeHatch = HS_VERTICAL;
ShapeType shapeType = SHAPE_ARROW;

HDC hMemDC = NULL;
HBITMAP hBitmap = NULL;
HBITMAP hOldBitmap = NULL;
int clientW = 0;
int clientH = 0;

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

    LoadStringW(hInstance, IDS_APP_TITLE, szTitle, MAX_LOADSTRING);
    wcscpy_s(szTitle, L"Nguyen Viet Hoang - 231230791");
    LoadStringW(hInstance, IDC_BAITHITHUGK, szWindowClass, MAX_LOADSTRING);
    MyRegisterClass(hInstance);

    if (!InitInstance(hInstance, nCmdShow))
    {
        return FALSE;
    }

    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_BAITHITHUGK));

    MSG msg;

    while (GetMessage(&msg, nullptr, 0, 0))
    {
        if (!TranslateAccelerator(msg.hwnd, hAccelTable, &msg))
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }

    return (int)msg.wParam;
}

ATOM MyRegisterClass(HINSTANCE hInstance)
{
    WNDCLASSEXW wcex;

    wcex.cbSize = sizeof(WNDCLASSEX);
    wcex.style = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc = WndProc;
    wcex.cbClsExtra = 0;
    wcex.cbWndExtra = 0;
    wcex.hInstance = hInstance;
    wcex.hIcon = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_BAITHITHUGK));
    wcex.hCursor = LoadCursor(nullptr, IDC_ARROW);
    wcex.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wcex.lpszMenuName = MAKEINTRESOURCEW(IDR_MENU1);
    wcex.lpszClassName = szWindowClass;
    wcex.hIconSm = LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_SMALL));

    return RegisterClassExW(&wcex);
}

BOOL InitInstance(HINSTANCE hInstance, int nCmdShow)
{
    hInst = hInstance;

    HWND hWnd = CreateWindowW(szWindowClass, szTitle, WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, nullptr, nullptr, hInstance, nullptr);

    if (!hWnd)
    {
        return FALSE;
    }

    ShowWindow(hWnd, nCmdShow);
    UpdateWindow(hWnd);

    SetTimer(hWnd, timerID, 1000, NULL);

    RECT rc;
    GetClientRect(hWnd, &rc);
    clientW = rc.right - rc.left;
    clientH = rc.bottom - rc.top;

    HDC hdc = GetDC(hWnd);
    hMemDC = CreateCompatibleDC(hdc);
    hBitmap = CreateCompatibleBitmap(hdc, clientW, clientH);
    hOldBitmap = (HBITMAP)SelectObject(hMemDC, hBitmap);

    HBRUSH hBgBrush = (HBRUSH)GetStockObject(WHITE_BRUSH);
    FillRect(hMemDC, &rc, hBgBrush);

    ReleaseDC(hWnd, hdc);

    return TRUE;
}

void DrawRectangleShape(HDC hdc, POINT p1, POINT p2)
{
    Rectangle(hdc,
        min(p1.x, p2.x),
        min(p1.y, p2.y),
        max(p1.x, p2.x),
        max(p1.y, p2.y));
}

void DrawTriangleShape(HDC hdc, POINT p1, POINT p2)
{
    int left = min(p1.x, p2.x);
    int right = max(p1.x, p2.x);
    int top = min(p1.y, p2.y);
    int bottom = max(p1.y, p2.y);

    POINT pts[3];
    pts[0] = { (left + right) / 2, top };
    pts[1] = { left, bottom };
    pts[2] = { right, bottom };

    Polygon(hdc, pts, 3);
}

void DrawArrowShape(HDC hdc, POINT p1, POINT p2)
{
    int left = min(p1.x, p2.x);
    int right = max(p1.x, p2.x);
    int top = min(p1.y, p2.y);
    int bottom = max(p1.y, p2.y);

    int w = right - left;
    int h = bottom - top;

    if (w < 30 || h < 20) return;

    POINT pts[7];

    if (p2.x >= p1.x)
    {
        pts[0] = { left, top + h / 4 };
        pts[1] = { left + w * 2 / 3, top + h / 4 };
        pts[2] = { left + w * 2 / 3, top };
        pts[3] = { right, top + h / 2 };
        pts[4] = { left + w * 2 / 3, bottom };
        pts[5] = { left + w * 2 / 3, top + h * 3 / 4 };
        pts[6] = { left, top + h * 3 / 4 };
    }
    else
    {
        pts[0] = { right, top + h / 4 };
        pts[1] = { left + w / 3, top + h / 4 };
        pts[2] = { left + w / 3, top };
        pts[3] = { left, top + h / 2 };
        pts[4] = { left + w / 3, bottom };
        pts[5] = { left + w / 3, top + h * 3 / 4 };
        pts[6] = { right, top + h * 3 / 4 };
    }

    Polygon(hdc, pts, 7);
}

void DrawShape(HDC hdc, POINT p1, POINT p2, ShapeType type)
{
    switch (type)
    {
    case SHAPE_RECT:
        DrawRectangleShape(hdc, p1, p2);
        break;
    case SHAPE_TRIANGLE:
        DrawTriangleShape(hdc, p1, p2);
        break;
    case SHAPE_ARROW:
        DrawArrowShape(hdc, p1, p2);
        break;
    }
}

void DrawShapeToMemory()
{
    if (!hMemDC) return;

    HPEN hPen = CreatePen(PS_SOLID, 2, RGB(0, 0, 0));
    HBRUSH hBrush = CreateHatchBrush(shapeHatch, shapeFillColor);

    HPEN oldPen = (HPEN)SelectObject(hMemDC, hPen);
    HBRUSH oldBrush = (HBRUSH)SelectObject(hMemDC, hBrush);

    DrawShape(hMemDC, startPt, endPt, shapeType);

    SelectObject(hMemDC, oldPen);
    SelectObject(hMemDC, oldBrush);

    DeleteObject(hPen);
    DeleteObject(hBrush);
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    switch (message)
    {
    case WM_COMMAND:
    {
        int wmId = LOWORD(wParam);

        switch (wmId)
        {
        case IDM_ABOUT:
            DialogBox(hInst, MAKEINTRESOURCE(IDD_ABOUTBOX), hWnd, About);
            break;

        case IDM_EXIT:
            DestroyWindow(hWnd);
            break;

        case ID_XANHDUONG:
            currentFillColor = RGB(0, 0, 255);
            break;

        case ID_VANG:
            currentFillColor = RGB(255, 255, 0);
            break;

        case ID_VERTICAL:
            currentHatch = HS_VERTICAL;
            break;

        case ID_CROSS:
            currentHatch = HS_CROSS;
            break;

        case ID_HCN:
            currentShape = SHAPE_RECT;
            break;

        case ID_TAMGIAC:
            currentShape = SHAPE_TRIANGLE;
            break;

        case ID_MUITEN:
            currentShape = SHAPE_ARROW;
            break;

        default:
            return DefWindowProc(hWnd, message, wParam, lParam);
        }
    }
    break;

    case WM_LBUTTONDOWN:
    {
        startPt.x = LOWORD(lParam);
        startPt.y = HIWORD(lParam);
        endPt = startPt;
        isDragging = true;
        hasShape = true;

        shapeFillColor = currentFillColor;
        shapeHatch = currentHatch;
        shapeType = currentShape;

        SetCapture(hWnd);
    }
    break;

    case WM_RBUTTONUP:
    {
        POINT pt;
        GetCursorPos(&pt);

        HMENU hMenu = LoadMenu(hInst, MAKEINTRESOURCE(IDR_MENU2));
        if (hMenu)
        {
            HMENU hPopup = GetSubMenu(hMenu, 0);
            if (hPopup)
            {
                TrackPopupMenu(hPopup, TPM_RIGHTBUTTON, pt.x, pt.y, 0, hWnd, NULL);
            }
            DestroyMenu(hMenu);
        }
    }
    break;

    case WM_MOUSEMOVE:
    {
        if (isDragging)
        {
            endPt.x = LOWORD(lParam);
            endPt.y = HIWORD(lParam);
            InvalidateRect(hWnd, NULL, TRUE);
        }
    }
    break;

    case WM_LBUTTONUP:
    {
        if (isDragging)
        {
            endPt.x = LOWORD(lParam);
            endPt.y = HIWORD(lParam);
            isDragging = false;
            ReleaseCapture();

            DrawShapeToMemory();   
            InvalidateRect(hWnd, NULL, TRUE);
        }
    }
    break;

    case WM_PAINT:
    {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hWnd, &ps);

        if (hMemDC)
        {
            BitBlt(hdc, 0, 0, clientW, clientH, hMemDC, 0, 0, SRCCOPY);
        }

        // nếu đang kéo chuột thì vẽ xem trước hình hiện tại
        if (isDragging)
        {
            HPEN hPen = CreatePen(PS_SOLID, 2, RGB(0, 0, 0));
            HBRUSH hBrush = CreateHatchBrush(shapeHatch, shapeFillColor);

            HPEN oldPen = (HPEN)SelectObject(hdc, hPen);
            HBRUSH oldBrush = (HBRUSH)SelectObject(hdc, hBrush);

            DrawShape(hdc, startPt, endPt, shapeType);

            SelectObject(hdc, oldPen);
            SelectObject(hdc, oldBrush);

            DeleteObject(hPen);
            DeleteObject(hBrush);
        }

        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        WCHAR timeText[20];
        wsprintf(timeText, L"%02d:%02d", minutes, seconds);

        RECT rc;
        GetClientRect(hWnd, &rc);
        rc.right -= 10;
        rc.bottom -= 10;

        SetBkMode(hdc, TRANSPARENT);
        DrawText(hdc, timeText, -1, &rc, DT_RIGHT | DT_BOTTOM | DT_SINGLELINE);

        EndPaint(hWnd, &ps);
    }
    break;
    case WM_TIMER:
    {
        if (wParam == timerID)
        {
            if (remainingSeconds > 0)
            {
                remainingSeconds--;
                InvalidateRect(hWnd, NULL, TRUE);
            }
            else
            {
                KillTimer(hWnd, timerID);
            }
        }
    }
    break;

    case WM_DESTROY:
        KillTimer(hWnd, timerID);

        if (hMemDC)
        {
            SelectObject(hMemDC, hOldBitmap);
            DeleteObject(hBitmap);
            DeleteDC(hMemDC);
        }

        PostQuitMessage(0);
        break;
    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

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