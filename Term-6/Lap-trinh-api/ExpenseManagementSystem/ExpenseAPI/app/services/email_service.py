import smtplib
import socket
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

from app.core.config import settings


class EmailDeliveryError(Exception):
    pass


class EmailService:
    def _get_smtp_credentials(self) -> tuple[str, str]:
        email_user = (settings.EMAIL_USER or "").strip()
        email_password = (settings.EMAIL_PASSWORD or "").strip().replace(" ", "")

        if not email_user or not email_password:
            raise EmailDeliveryError("Cấu hình EMAIL_USER hoặc EMAIL_PASSWORD đang trống.")

        return email_user, email_password

    def send_otp_email(self, to_email: str, otp: str):
        email_user, email_password = self._get_smtp_credentials()

        subject = "OTP Verification"
        body = f"""
Your OTP is: {otp}
This code will expire in 5 minutes.
"""

        message = MIMEMultipart()
        message["From"] = email_user
        message["To"] = to_email
        message["Subject"] = subject
        message.attach(MIMEText(body, "plain"))

        server = None
        try:
            print("📧 Sending email to:", to_email)

            server = smtplib.SMTP(settings.EMAIL_HOST, settings.EMAIL_PORT, timeout=20)
            server.ehlo()
            server.starttls()
            server.ehlo()

            print("🔐 Logging in Gmail...")
            server.login(email_user, email_password)

            print("📤 Sending message...")
            server.send_message(message)

            print("✅ Email sent successfully!")

        except smtplib.SMTPAuthenticationError as e:
            print("❌ EMAIL AUTH ERROR:", str(e))
            raise EmailDeliveryError(
                "Không thể đăng nhập Gmail SMTP. Hãy kiểm tra EMAIL_USER / EMAIL_PASSWORD "
                "(đặc biệt là App Password không được chứa khoảng trắng)."
            )
        except (smtplib.SMTPException, socket.timeout, OSError) as e:
            print("❌ EMAIL SMTP ERROR:", str(e))
            raise EmailDeliveryError("Không thể gửi email OTP vào lúc này. Vui lòng thử lại sau.")
        except Exception as e:
            print("❌ EMAIL ERROR:", str(e))
            raise EmailDeliveryError("Đã xảy ra lỗi không xác định khi gửi email OTP.")
        finally:
            if server is not None:
                try:
                    server.quit()
                except Exception:
                    pass

    def send_reset_password_email(self, to_email: str, token: str):
        email_user, email_password = self._get_smtp_credentials()

        subject = "Reset Password"
        link = f"http://localhost:8000/reset-password?token={token}"

        body = f"""
Click the link below to reset your password:

{link}

This link will expire in 15 minutes.
"""

        message = MIMEMultipart()
        message["From"] = email_user
        message["To"] = to_email
        message["Subject"] = subject
        message.attach(MIMEText(body, "plain"))

        server = None
        try:
            server = smtplib.SMTP(settings.EMAIL_HOST, settings.EMAIL_PORT, timeout=20)
            server.ehlo()
            server.starttls()
            server.ehlo()
            server.login(email_user, email_password)
            server.send_message(message)
        except smtplib.SMTPAuthenticationError:
            raise EmailDeliveryError(
                "Không thể đăng nhập Gmail SMTP để gửi email đặt lại mật khẩu."
            )
        except Exception as e:
            print("❌ Reset email error:", e)
            raise EmailDeliveryError("Không thể gửi email đặt lại mật khẩu.")
        finally:
            if server is not None:
                try:
                    server.quit()
                except Exception:
                    pass