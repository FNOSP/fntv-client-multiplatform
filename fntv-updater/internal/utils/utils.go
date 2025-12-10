package utils

import (
	"runtime"
	"strings"
)

// IsWindows checks if we are running on Windows
func IsWindows() bool {
	return strings.Contains(strings.ToLower(runtime.GOOS), "windows")
}