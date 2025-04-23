import os
from PIL import Image, UnidentifiedImageError
import tkinter as tk
# from tkinter import ttk # No longer needed directly
from tkinter import filedialog, messagebox # Keep messagebox
# import scrolledtext # No longer needed
import customtkinter as ctk # Import customtkinter
import threading

# 定义支持的图片扩展名
SUPPORTED_EXTENSIONS = ['.png', '.gif', '.bmp', '.jpg', '.jpeg']
# 定义 ICO 分辨率 (Pillow save 支持直接传入 sizes 列表)
ICO_RESOLUTIONS = [(256, 256), (128, 128), (96, 96), (64, 64), (48, 48), (32, 32), (24, 24), (16, 16)]

def convert_image_to_ico(image_path, output_path):
    """使用 Pillow 将单个图片转换为 ICO (移除 print，错误通过异常传递)"""
    # print(f"- 正在转换: {os.path.basename(image_path)}") # Removed print
    try:
        img = Image.open(image_path)
        # Pillow 的 save 方法可以直接指定 sizes 参数来生成多分辨率 ICO
        img.save(output_path, format='ICO', sizes=ICO_RESOLUTIONS)
        # print(f"  成功: {os.path.basename(output_path)}") # Removed print
    except FileNotFoundError as e:
        # print(f"  错误: 文件未找到 - {image_path}") # Removed print
        raise FileNotFoundError(f"文件未找到 - {image_path}") from e
    except UnidentifiedImageError as e:
        # print(f"  错误: 无法识别的图像文件或格式不支持 - {os.path.basename(image_path)}") # Removed print
        raise UnidentifiedImageError(f"无法识别或不支持的图像文件 - {os.path.basename(image_path)}") from e
    except Exception as e:
        # print(f"  转换失败: {os.path.basename(image_path)}") # Removed print
        # print(f"  错误: {e}") # Removed print
        # 在 GUI 版本中，将错误信息包装后重新抛出
        raise type(e)(f"转换失败 ({os.path.basename(image_path)}): {e}") from e


# --- Setup CustomTkinter ---
ctk.set_appearance_mode("System")  # Modes: "System" (default), "Dark", "Light"
ctk.set_default_color_theme("blue")  # Themes: "blue" (default), "green", "dark-blue"

# --- GUI Application Class ---
class PngToIcoApp(ctk.CTk): # Inherit from ctk.CTk
    def __init__(self):
        super().__init__() # Initialize the CTk window

        self.title("图片转 ICO 工具 (CustomTkinter)")
        self.geometry("650x500") # Adjust window size slightly

        self.input_paths = [] # Store list of selected input file paths or a single directory path
        self.output_dir = ""

        # --- Configure grid layout ---
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(2, weight=1) # Allow status area to expand

        # --- Widgets (using customtkinter) ---
        # Input Selection Frame
        input_frame = ctk.CTkFrame(self)
        input_frame.grid(row=0, column=0, padx=10, pady=(10, 5), sticky="ew")
        input_frame.grid_columnconfigure(1, weight=1)

        input_label_widget = ctk.CTkLabel(input_frame, text="输入图片/文件夹:", anchor="w")
        input_label_widget.grid(row=0, column=0, padx=(10, 5), pady=10)
        self.input_entry = ctk.CTkEntry(input_frame, placeholder_text="尚未选择", state="readonly", width=350)
        self.input_entry.grid(row=0, column=1, padx=5, pady=10, sticky="ew")
        input_button = ctk.CTkButton(input_frame, text="选择...", command=self.select_input, width=80)
        input_button.grid(row=0, column=2, padx=(5, 10), pady=10)

        # Output Selection Frame
        output_frame = ctk.CTkFrame(self)
        output_frame.grid(row=1, column=0, padx=10, pady=5, sticky="ew")
        output_frame.grid_columnconfigure(1, weight=1)

        output_label_widget = ctk.CTkLabel(output_frame, text="输出目录:", anchor="w")
        output_label_widget.grid(row=0, column=0, padx=(10, 28), pady=10) # Adjust padding for alignment
        self.output_entry = ctk.CTkEntry(output_frame, placeholder_text="尚未选择", state="readonly", width=350)
        self.output_entry.grid(row=0, column=1, padx=5, pady=10, sticky="ew")
        output_button = ctk.CTkButton(output_frame, text="选择...", command=self.select_output, width=80)
        output_button.grid(row=0, column=2, padx=(5, 10), pady=10)

        # Status Area Frame (using CTkTextbox)
        status_frame = ctk.CTkFrame(self)
        status_frame.grid(row=2, column=0, padx=10, pady=5, sticky="nsew")
        status_frame.grid_rowconfigure(1, weight=1)
        status_frame.grid_columnconfigure(0, weight=1)

        status_label_widget = ctk.CTkLabel(status_frame, text="状态:", anchor="w")
        status_label_widget.grid(row=0, column=0, padx=10, pady=(5, 0), sticky="w")
        self.status_text = ctk.CTkTextbox(status_frame, wrap=tk.WORD, state=tk.DISABLED, corner_radius=5) # Use CTkTextbox
        self.status_text.grid(row=1, column=0, padx=10, pady=(0, 10), sticky="nsew")

        # Convert Button Frame
        button_frame = ctk.CTkFrame(self, fg_color="transparent") # Transparent background
        button_frame.grid(row=3, column=0, padx=10, pady=10)
        self.convert_button = ctk.CTkButton(button_frame, text="开始转换", command=self.start_conversion_thread, state=tk.DISABLED)
        self.convert_button.pack()

    def log_status(self, message):
        """Appends a message to the status CTkTextbox (thread-safe)."""
        def append():
            self.status_text.configure(state=tk.NORMAL) # Use configure for CTkTextbox
            self.status_text.insert(tk.END, message + "\n")
            self.status_text.see(tk.END) # Scroll to the end
            self.status_text.configure(state=tk.DISABLED)
        # Schedule the GUI update to run in the main Tkinter thread
        # Use self (the CTk root window) instead of self.master
        self.after(0, append)

    def select_input(self):
        """Opens dialog to select input files or a directory."""
        # Ask for files first
        filenames = filedialog.askopenfilenames(
            title="选择一个或多个图片文件",
            filetypes=[("图片文件", "*.png *.gif *.bmp *.jpg *.jpeg"), ("所有文件", "*.*")]
        )
        if filenames:
            self.input_paths = list(filenames)
            # Update CTkEntry by configuring its state, setting value, then disabling again
            self.input_entry.configure(state=tk.NORMAL)
            self.input_entry.delete(0, tk.END)
            self.input_entry.insert(0, f"{len(self.input_paths)} 个文件已选择")
            self.input_entry.configure(state="readonly")
            self.log_status(f"已选择输入文件: {len(self.input_paths)} 个")
        else:
            # If no files selected, ask for directory
            dirname = filedialog.askdirectory(title="或选择包含图片的文件夹")
            if dirname:
                self.input_paths = [dirname] # Store directory path as a list with one element
                self.input_entry.configure(state=tk.NORMAL)
                self.input_entry.delete(0, tk.END)
                self.input_entry.insert(0, dirname)
                self.input_entry.configure(state="readonly")
                self.log_status(f"已选择输入文件夹: {dirname}")
            # else: # User cancelled both selections
                 # self.input_paths = []
                 # self.input_entry.configure(state=tk.NORMAL)
                 # self.input_entry.delete(0, tk.END)
                 # self.input_entry.insert(0, "尚未选择") # Or placeholder_text handles this
                 # self.input_entry.configure(state="readonly")

        self.check_ready_to_convert()


    def select_output(self):
        """Opens dialog to select the output directory."""
        dirname = filedialog.askdirectory(title="选择保存 ICO 文件的目录")
        if dirname:
            self.output_dir = dirname
            self.output_entry.configure(state=tk.NORMAL)
            self.output_entry.delete(0, tk.END)
            self.output_entry.insert(0, self.output_dir)
            self.output_entry.configure(state="readonly")
            self.log_status(f"已选择输出目录: {self.output_dir}")
        self.check_ready_to_convert()

    def check_ready_to_convert(self):
        """Enables the convert button if input and output are selected."""
        if self.input_paths and self.output_dir:
            self.convert_button.configure(state=tk.NORMAL) # Use configure for CTkButton
        else:
            self.convert_button.configure(state=tk.DISABLED)

    def start_conversion_thread(self):
        """Starts the conversion process in a separate thread."""
        if not self.input_paths or not self.output_dir:
            messagebox.showerror("错误", "请先选择输入和输出路径！")
            return

        self.convert_button.configure(state=tk.DISABLED) # Use configure
        self.status_text.configure(state=tk.NORMAL) # Use configure
        self.status_text.delete('1.0', tk.END) # Clear previous status
        self.status_text.configure(state=tk.DISABLED) # Use configure
        self.log_status("开始转换...")

        # Run the conversion logic in a separate thread
        thread = threading.Thread(target=self.run_conversion, daemon=True)
        thread.start()

    def run_conversion(self):
        """The actual conversion logic executed in the background thread."""
        files_to_convert = []
        # Check if input is a directory
        if len(self.input_paths) == 1 and os.path.isdir(self.input_paths[0]):
            input_dir = self.input_paths[0]
            self.log_status(f"扫描文件夹: {input_dir}")
            try:
                for filename in os.listdir(input_dir):
                    _, file_ext = os.path.splitext(filename)
                    if file_ext.lower() in SUPPORTED_EXTENSIONS:
                        files_to_convert.append(os.path.join(input_dir, filename))
            except OSError as e:
                 self.log_status(f"错误：无法读取文件夹 {input_dir}: {e}")
                 self.after(0, lambda: self.convert_button.configure(state=tk.NORMAL)) # Re-enable button on error
                 return
        else: # Input is a list of files
            # Filter selected files just in case a non-supported one was somehow selected
            # (though filedialog should prevent this)
            files_to_convert = [f for f in self.input_paths if os.path.isfile(f) and os.path.splitext(f)[1].lower() in SUPPORTED_EXTENSIONS]


        if not files_to_convert:
            self.log_status("未找到支持的图片文件进行转换。")
            self.after(0, lambda: self.convert_button.configure(state=tk.NORMAL)) # Re-enable button
            return

        self.log_status(f"找到 {len(files_to_convert)} 个文件，开始处理...")
        success_count = 0
        fail_count = 0

        for input_file_path in files_to_convert:
            base_name = os.path.basename(input_file_path)
            file_name_no_ext, _ = os.path.splitext(base_name)
            output_ico_path = os.path.join(self.output_dir, f"{file_name_no_ext}.ico")
            try:
                self.log_status(f"- 正在转换: {base_name} -> {os.path.basename(output_ico_path)}")
                convert_image_to_ico(input_file_path, output_ico_path)
                self.log_status(f"  成功: {os.path.basename(output_ico_path)}")
                success_count += 1
            except Exception as e:
                self.log_status(f"  失败: {base_name} - {e}")
                fail_count += 1

        self.log_status("--------------------")
        self.log_status(f"转换完成。成功: {success_count}, 失败: {fail_count}")
        # Re-enable button in the main thread
        self.after(0, lambda: self.convert_button.configure(state=tk.NORMAL))


# --- Main Execution ---
if __name__ == "__main__":
    app = PngToIcoApp() # Create the CTk app instance
    app.mainloop()
