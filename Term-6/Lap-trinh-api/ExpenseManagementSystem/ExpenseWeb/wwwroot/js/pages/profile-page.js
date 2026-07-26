(function () {
    const defaultAvatar = '/sneat/img/avatars/default/teams_1.png';
    const form = document.getElementById('formProfileSettings');
    if (!form) return;

    const lastName = document.getElementById('lastName');
    const firstName = document.getElementById('firstName');
    const email = document.getElementById('email');
    const phoneNumber = document.getElementById('phoneNumber');
    const avatarUrl = document.getElementById('avatarUrl');
    const uploadedAvatar = document.getElementById('uploadedAvatar');
    const uploadAvatarFile = document.getElementById('uploadAvatarFile');
    const btnResetProfile = document.getElementById('btnResetProfile');
    const btnDeleteAccount = document.getElementById('btnDeleteAccount');

    const initialState = {
        lastName: lastName?.value || '',
        firstName: firstName?.value || '',
        email: email?.value || '',
        phoneNumber: phoneNumber?.value || '',
        avatar: avatarUrl?.value || ''
    };

    function updateAvatarPreview() {
        if (!uploadedAvatar || !avatarUrl) return;
        uploadedAvatar.src = avatarUrl.value.trim() || uploadedAvatar.src || defaultAvatar;
    }

    function restoreInitialState() {
        if (lastName) lastName.value = initialState.lastName;
        if (firstName) firstName.value = initialState.firstName;
        if (email) email.value = initialState.email;
        if (phoneNumber) phoneNumber.value = initialState.phoneNumber;
        if (avatarUrl) avatarUrl.value = initialState.avatar;
        if (uploadAvatarFile) uploadAvatarFile.value = '';
        updateAvatarPreview();
    }

    avatarUrl?.addEventListener('input', function () {
        if (uploadAvatarFile) uploadAvatarFile.value = '';
        updateAvatarPreview();
    });

    btnResetProfile?.addEventListener('click', restoreInitialState);

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        if (!lastName.value.trim() && !firstName.value.trim()) {
            alert('Vui lòng nhập họ tên.');
            return;
        }
        if (!email.value.trim()) {
            alert('Vui lòng nhập email.');
            return;
        }

        const formData = new FormData();
        formData.append('LastName', lastName.value.trim());
        formData.append('FirstName', firstName.value.trim());
        formData.append('Email', email.value.trim());
        formData.append('PhoneNumber', phoneNumber.value.trim());
        formData.append('Avatar', avatarUrl?.value.trim() || '');

        if (uploadAvatarFile && uploadAvatarFile.files.length > 0) {
            formData.append('AvatarFile', uploadAvatarFile.files[0]);
        }

        try {
            const response = await fetch('/Profile/UpdateAjax', {
                method: 'POST',
                body: formData
            });

            const data = await response.json();
            if (!response.ok || !data.success) {
                alert(data.message || 'Không thể cập nhật hồ sơ.');
                return;
            }

            alert(data.message || 'Cập nhật hồ sơ thành công.');
            window.location.reload();
        } catch (error) {
            alert('Đã xảy ra lỗi khi cập nhật hồ sơ.');
        }
    });

    btnDeleteAccount?.addEventListener('click', async function () {
        const ok = confirm('Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.');
        if (!ok) return;

        try {
            const response = await fetch('/Profile/DeleteAccountAjax', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            const data = await response.json();
            if (!response.ok || !data.success) {
                alert(data.message || 'Không thể xóa tài khoản.');
                return;
            }
            window.location.href = data.redirectUrl || '/Auth/Login';
        } catch (error) {
            alert('Đã xảy ra lỗi khi xóa tài khoản.');
        }
    });
})();
