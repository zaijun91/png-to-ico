; Inno Setup Script for PngToIco Converter

[Setup]
; 基本应用信息
AppName=PngToIco Converter
AppVersion=1.0
AppPublisher=MyCompany ; 您可以修改公司名称
DefaultDirName={autopf64}\PngToIco Converter ; 自动选择 Program Files (x86 或 64位)
DefaultGroupName=PngToIco Converter ; 开始菜单文件夹名称
AllowNoIcons=yes
; 输出安装包设置
OutputBaseFilename=PngToIco_Converter_Setup_v1.0
Compression=lzma
SolidCompression=yes
WizardStyle=modern
; PrivilegesRequired=admin ; Removed, let Inno Setup determine based on install path
UninstallDisplayIcon={app}\png_to_ico.exe
UninstallDisplayName=Uninstall PngToIco Converter

; [Languages]
; Name: "chinesesimp"; MessagesFile: "compiler:Languages\ChineseSimplified.isl" ; Removed due to missing file error. Installer will be in English.

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; 要包含的主程序文件 (源路径相对于 .iss 文件)
Source: "dist\png_to_ico.exe"; DestDir: "{app}"; Flags: ignoreversion
; 注意: 如果 PyInstaller 生成的是文件夹, 使用 Source: "dist\png_to_ico\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; 开始菜单快捷方式
Name: "{group}\PngToIco Converter"; Filename: "{app}\png_to_ico.exe"
Name: "{group}\{cm:UninstallProgram,PngToIco Converter}"; Filename: "{uninstallexe}"
; 桌面快捷方式 (如果用户在任务页面勾选)
Name: "{autodesktop}\PngToIco Converter"; Filename: "{app}\png_to_ico.exe"; Tasks: desktopicon

[Run]
; 可选：安装完成后运行程序
; Filename: "{app}\png_to_ico.exe"; Description: "{cm:LaunchProgram,PngToIco Converter}"; Flags: nowait postinstall skipifsilent
