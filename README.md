# PNG to ICO Converter (PNG 转 ICO 工具)

## 关于 (About)

这是一个简单易用的工具，可以方便地将 PNG、GIF、BMP、SVG 或 JPG 等格式的图片批量转换为多分辨率的 ICO 图标文件。它基于强大的 [ImageMagick](https://www.imagemagick.org/script/index.php) 图像处理库。

如果你不想处理复杂的命令行操作，也不想安装臃肿的专业软件，那么这个小工具非常适合你！

(English: [KISS](https://en.wikipedia.org/wiki/KISS_principle) batch script to easily convert PNG, GIF, BMP, SVG or JPG images to multi-resolution [ICO](https://en.wikipedia.org/wiki/ICO_(file_format)) files using [ImageMagick](https://www.imagemagick.org/script/index.php). Don't want to deal with Command Line or install a heavy, bloated program ? Well, you're in the right place !)

## 技术栈 (Technology Stack)

*   **核心转换:** [ImageMagick](https://www.imagemagick.org/script/index.php) (使用其 `convert` 命令)
*   **脚本封装 (可选):** Python (用于 `png_to_ico.py`)，Windows 批处理 (`png_to_ico.bat`)
*   **打包:** [PyInstaller](https://pyinstaller.org/en/stable/) (用于将 Python 脚本打包成 `.exe`)
*   **安装包制作:** [Inno Setup](https://jrsoftware.org/isinfo.php) (用于创建 Windows 安装程序)

## 如何使用 (Usage)

### 推荐方式：使用安装包 (For Regular Users)

我们强烈推荐普通用户下载并使用我们制作的安装包，这是最简单方便的方式。

1.  **下载安装包:**
    *   点击下面的链接直接下载最新版本的安装程序：
    *   [**下载 PngToIco_Converter_Setup_v1.0.exe**](https://github.com/zaijun91/png-to-ico/raw/master/Output/PngToIco_Converter_Setup_v1.0.exe)
    *   (您也可以在仓库的 `Output` 文件夹下找到它)

2.  **运行安装程序:** 下载完成后，双击运行 `PngToIco_Converter_Setup_v1.0.exe` 文件，按照提示完成安装。安装过程会自动将程序添加到右键菜单。

3.  **转换图片:**
    *   找到您想要转换的图片文件 (PNG, GIF, BMP, SVG, JPG 等) 或包含这些图片的文件夹。
    *   **右键点击** 该文件或文件夹。
    *   在弹出的菜单中，选择 **"PngToIco Converter"** (或类似的选项，具体名称可能因安装设置略有不同)。
    *   稍等片刻，转换后的 `.ico` 图标文件就会出现在与原图片相同的文件夹中。

    ![](demo/Demo.gif)

### 其他方式 (Alternative Methods)

*   **直接运行 .bat (不安装):** 如果您不想安装，可以直接下载仓库中的 `png_to_ico.bat` 文件和 `ImageMagick` 文件夹。然后，将单个图片文件或包含图片的文件夹 **拖拽** 到 `png_to_ico.bat` 文件上即可开始转换。
*   **使用 Python 脚本 (开发者):** 如果您安装了 Python 环境，可以直接运行 `png_to_ico.py` 脚本，并将图片文件或文件夹路径作为参数传递给它。

## 功能特性 (Features)

*   生成的 ICO 文件默认包含多种分辨率：256x256, 128x128, 96x96, 64x64, 48x48, 32x32, 24x24, 和 16x16 像素。
*   转换后的图标文件会自动保存在原图片所在的文件夹，并使用相同的文件名（扩展名为 .ico）。
*   内置了稳定版本的 ImageMagick `convert` 程序，无需额外安装。

## 系统要求 (Requirements)

*   Microsoft Windows

## 觉得好用？点个 Star 吧！(Like it? Give it a Star!)

如果您觉得这个小工具对您有帮助，请在 GitHub 仓库页面给我们点一个 Star ⭐！您的支持是我们改进的最大动力！谢谢！
(If you find this tool useful, please give us a Star ⭐ on the GitHub repository page! Your support is our greatest motivation! Thank you!)

## 许可证 (License)

本项目使用 [Unlicense](http://unlicense.org) 许可证发布，您可以自由使用、修改和分发。
PNG to ICO is released under the [Unlicense](http://unlicense.org).
