package ui

import (
	"unsafe"

	"fntv_updater/internal/utils"

	"golang.org/x/sys/windows"
)

// ShowMessageBox displays a Windows message box
func ShowMessageBox(message, title string, flags uint) {
	if !utils.IsWindows() {
		return
	}

	// Convert strings to UTF16 pointers
	messagePtr, _ := windows.UTF16PtrFromString(message)
	titlePtr, _ := windows.UTF16PtrFromString(title)

	// Show message box
	user32 := windows.NewLazySystemDLL("user32.dll")
	MessageBox := user32.NewProc("MessageBoxW")
	MessageBox.Call(
		0,
		uintptr(unsafe.Pointer(messagePtr)),
		uintptr(unsafe.Pointer(titlePtr)),
		uintptr(flags),
	)
}
