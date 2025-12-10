package main

import (
	"os"
	"os/exec"
	"path/filepath"
	"time"

	"fntv_updater/internal/consts"
	"fntv_updater/internal/installer"
	"fntv_updater/internal/lang"
	"fntv_updater/internal/logger"
	"fntv_updater/internal/process"
)

func main() {
	logger.InitLog()
	if logger.LogFile != nil {
		defer logger.LogFile.Close()
	}

	defer func() {
		if r := recover(); r != nil {
			logger.ErrorLog("Updater", lang.Msg("error_occurred", r))
			end()
		}
	}()

	logger.Info("Updater", lang.Msg("updater_started"))

	args := os.Args
	if len(args) < 3 {
		logger.Info("Updater", lang.Msg("usage", args[0]))
		end()
		return
	}

	installerPath := args[1]
	installDir := args[2]

	if !installer.CheckRegistryInstall() {
		logger.Info("Updater", "Application not found in registry. Starting interactive installation...")
		cmd := exec.Command(installerPath)
		if err := cmd.Run(); err != nil {
			logger.ErrorLog("Updater", "Installer failed: %v", err)
			end()
			return
		}
		os.Exit(0)
	}

	logger.Info("Updater", lang.Msg("wait_app_exit"))
	if err := process.WaitAppExit(); err == nil {
		logger.Info("Updater", lang.Msg("app_closed"))
	}

	logger.Info("Updater", "Cleaning installation directory: %s", installDir)
	if err := installer.CleanInstallDir(installDir, installerPath); err != nil {
		logger.ErrorLog("Updater", "Failed to clean directory: %v", err)
		// Continue anyway? The user requirement says "clean up... then execute".
		// If cleanup fails, we might still want to try installing.
	}

	logger.Info("Updater", "Starting installer: %s", installerPath)
	// Execute "FnMedia_Setup_xxx.exe /SILENT /SP- /SUPPRESSMSGBOXES /NORESTART /CLOSEAPPLICATIONS"
	cmd := exec.Command(installerPath, "/SILENT", "/SP-", "/SUPPRESSMSGBOXES", "/NORESTART", "/CLOSEAPPLICATIONS")

	if err := cmd.Run(); err != nil {
		logger.ErrorLog("Updater", "Installer failed: %v", err)
		end()
		return
	}

	logger.Success("Updater", "Installation completed successfully.")

	logger.Info("Updater", "Deleting installer file: %s", installerPath)
	if err := os.Remove(installerPath); err != nil {
		logger.Info("Updater", "Failed to delete installer file: %v", err)
	} else {
		logger.Success("Updater", "Installer file deleted successfully.")
	}

	// Launch the app
	appName := consts.AppName
	appPath := filepath.Join(installDir, appName)

	logger.Info("Updater", "Launching application: %s", appPath)

	appCmd := exec.Command(appPath)
	if err := appCmd.Start(); err != nil {
		logger.ErrorLog("Updater", "Failed to launch application: %v", err)
	} else {
		logger.Success("Updater", "Application launched successfully.")
	}

	os.Exit(0)
}

func end() {
	logger.Info("Updater", lang.Msg("end"))
	time.Sleep(3 * time.Second)
	os.Exit(0)
}
