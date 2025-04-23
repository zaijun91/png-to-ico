; NSIS Script for PngToIco Converter

!define APP_NAME "PngToIco Converter"
!define COMPANY_NAME "MyCompany" ; You can change the company name here
!define VERSION "1.0"
!define EXE_NAME "png_to_ico.exe"
!define INSTALLER_NAME "PngToIco_Converter_Setup_v${VERSION}.exe"
!define UNINSTALLER_NAME "Uninstall PngToIco Converter.exe"
!define MAIN_APP_FILENAME "dist\${EXE_NAME}" ; Path relative to the .nsi file

;--------------------------------
; 基本设置

Name "${APP_NAME}"
OutFile "${INSTALLER_NAME}"
InstallDir "$PROGRAMFILES64\${APP_NAME}" ; Default installation directory (64-bit)
InstallDirRegKey HKLM "Software\${COMPANY_NAME}\${APP_NAME}" "Install_Dir"
RequestExecutionLevel admin ; Request administrator privileges

;--------------------------------
; 界面设置

!include MUI2.nsh ; 使用现代用户界面库
!define MUI_ABORTWARNING ; 如果用户中止安装，显示警告

!insertmacro MUI_PAGE_WELCOME
; !insertmacro MUI_PAGE_LICENSE "LICENSE" ; Temporarily removed again due to persistent encoding issue
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; !insertmacro MUI_LANGUAGE "SimpChinese" ; Removed again due to persistent encoding issue. Installer will be in English.

;--------------------------------
; 安装程序区段

Section "Install ${APP_NAME}" SEC_INSTALL
  SetOutPath $INSTDIR

  ; 复制主程序文件
  File "${MAIN_APP_FILENAME}"

  ; --- 可选：如果 PyInstaller 生成的是文件夹而不是单文件，则使用 File /r ---
  ; File /r "dist\png_to_ico\" ; 复制 dist 文件夹下的所有内容

  ; 写入卸载信息到注册表
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayName" "${APP_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "UninstallString" '"$INSTDIR\${UNINSTALLER_NAME}"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayIcon" '"$INSTDIR\${EXE_NAME}"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "Publisher" "${COMPANY_NAME}"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "NoRepair" 1
  WriteRegStr HKLM "Software\${COMPANY_NAME}\${APP_NAME}" "Install_Dir" "$INSTDIR"

  ; 创建卸载程序
  WriteUninstaller "$INSTDIR\${UNINSTALLER_NAME}"

  ; 创建开始菜单快捷方式
  CreateDirectory "$SMPROGRAMS\${APP_NAME}"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" "$INSTDIR\${EXE_NAME}"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\Uninstall ${APP_NAME}.lnk" "$INSTDIR\${UNINSTALLER_NAME}" ; Changed to English

  ; 可选：创建桌面快捷方式
  ; CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\${EXE_NAME}"

SectionEnd

;--------------------------------
; 卸载程序区段

Section "Uninstall" SEC_UNINSTALL
  ; 删除文件
  Delete "$INSTDIR\${EXE_NAME}"
  Delete "$INSTDIR\${UNINSTALLER_NAME}"
  ; --- 可选：如果安装的是文件夹，则使用 RMDir /r ---
  ; RMDir /r "$INSTDIR" ; 如果安装了整个文件夹

  ; 删除快捷方式
  Delete "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk"
  Delete "$SMPROGRAMS\${APP_NAME}\Uninstall ${APP_NAME}.lnk" ; Changed to English
  RMDir "$SMPROGRAMS\${APP_NAME}" ; 删除开始菜单文件夹

  ; 可选：删除桌面快捷方式
  ; Delete "$DESKTOP\${APP_NAME}.lnk"

  ; 删除注册表卸载信息
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
  DeleteRegKey HKLM "Software\${COMPANY_NAME}\${APP_NAME}"

  ; 如果安装目录为空，则尝试删除
  RMDir "$INSTDIR"
SectionEnd

;--------------------------------
; 函数 (可选，例如检查是否已运行)

Function .onInit
  ; 可选：检查程序是否已在运行
FunctionEnd
