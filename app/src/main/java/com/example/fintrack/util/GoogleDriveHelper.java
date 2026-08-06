package com.example.fintrack.util;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;

// Import cho Bước 5 & 6 (Drive Client & File Operations)
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;

public class GoogleDriveHelper {

    private final GoogleSignInClient mGoogleSignInClient;
    private final Context context;

    // =========================================================================
    // BƯỚC 4: KHỞI TẠO YÊU CẦU ĐĂNG NHẬP & XIN QUYỀN GOOGLE DRIVE
    // =========================================================================
    public GoogleDriveHelper(Context context) {
        this.context = context;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void requestSignIn(ActivityResultLauncher<Intent> launcher) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launcher.launch(signInIntent);
    }

    public void signOut(Runnable onComplete) {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> onComplete.run());
    }

    public GoogleSignInAccount getSignedInAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(DriveScopes.DRIVE_APPDATA))) {
            return account;
        }
        return null;
    }

    // =========================================================================
    // BƯỚC 5: KHỞI TẠO ĐỐI TƯỢNG DRIVE CLIENT
    // =========================================================================
    public Drive getDriveService(GoogleSignInAccount account) {
        if (account == null) return null;

        // 1. Tạo Credential từ tài khoản Google
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_APPDATA)
        );
        credential.setSelectedAccount(account.getAccount());

        // 2. Khởi tạo và trả về đối tượng Drive Client
        return new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("FinTrack")
                .build();
    }

    // =========================================================================
    // BƯỚC 6: THAO TÁC ĐỌC, GHI (UPLOAD/DOWNLOAD) FILE TRÊN GOOGLE DRIVE
    // =========================================================================

    /**
     * 1. GHI (UPLOAD) FILE LÊN GOOGLE DRIVE
     */
    public void uploadFileToDrive(GoogleSignInAccount account, java.io.File localFile, String mimeType, DriveCallback<String> callback) {
        new Thread(() -> {
            try {
                Drive driveService = getDriveService(account);
                if (driveService == null) {
                    callback.onError(new Exception("Chưa đăng nhập Google"));
                    return;
                }

                // Cấu hình vị trí lưu là thư mục ẩn appDataFolder
                File fileMetadata = new File();
                fileMetadata.setName(localFile.getName());
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));

                FileContent mediaContent = new FileContent(mimeType, localFile);

                File uploadedFile = driveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id, name")
                        .execute();

                callback.onSuccess(uploadedFile.getId());
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    /**
     * 2. ĐỌC (DOWNLOAD) FILE TỪ GOOGLE DRIVE VỀ MÁY LOCAL
     */
    public void downloadFileFromDrive(GoogleSignInAccount account, String fileId, java.io.File targetLocalFile, DriveCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                Drive driveService = getDriveService(account);
                if (driveService == null) {
                    callback.onError(new Exception("Chưa đăng nhập Google"));
                    return;
                }

                OutputStream outputStream = new FileOutputStream(targetLocalFile);
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                outputStream.flush();
                outputStream.close();

                callback.onSuccess(true);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    /**
     * 3. TÌM KIẾM FILE ĐÃ SAO LƯU TRÊN DRIVE THEO TÊN FILE
     */
    public void findBackupFile(GoogleSignInAccount account, String fileName, DriveCallback<String> callback) {
        new Thread(() -> {
            try {
                Drive driveService = getDriveService(account);
                if (driveService == null) {
                    callback.onError(new Exception("Chưa đăng nhập Google"));
                    return;
                }

                String query = "name = '" + fileName + "' and 'appDataFolder' in parents and trashed = false";
                FileList resultList = driveService.files().list()
                        .setSpaces("appDataFolder")
                        .setQ(query)
                        .setFields("files(id, name)")
                        .execute();

                if (resultList.getFiles() != null && !resultList.getFiles().isEmpty()) {
                    callback.onSuccess(resultList.getFiles().get(0).getId());
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    /**
     * Interface Callback nhận kết quả từ các luồng phụ (Thread)
     */
    public interface DriveCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}