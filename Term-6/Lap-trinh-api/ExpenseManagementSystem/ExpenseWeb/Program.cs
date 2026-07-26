using ExpenseWeb.Services.Api;
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);

var culture = new CultureInfo("en-US");
CultureInfo.DefaultThreadCurrentCulture = culture;
CultureInfo.DefaultThreadCurrentUICulture = culture;

builder.Services.AddControllersWithViews();
builder.Services.AddHttpContextAccessor();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(30);
    options.Cookie.HttpOnly = true;
    options.Cookie.IsEssential = true;
});

static IHttpClientBuilder ConfigureApiClient<T>(WebApplicationBuilder b) where T : class =>
    b.Services.AddHttpClient<T>(client =>
    {
        client.BaseAddress = new Uri(b.Configuration["ApiSettings:BaseUrl"]!);
        client.Timeout = TimeSpan.FromSeconds(120);
    });

ConfigureApiClient<AuthApiService>(builder);
ConfigureApiClient<CategoryApiService>(builder);
ConfigureApiClient<WalletApiService>(builder);
ConfigureApiClient<TransactionApiService>(builder);
ConfigureApiClient<ReportApiService>(builder);
ConfigureApiClient<SupportApiService>(builder);
ConfigureApiClient<AdminApiService>(builder); 

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseSession();
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Auth}/{action=Login}/{id?}");

app.Run();