package process

import (
	"fmt"
	"strings"
	"time"

	"fntv_updater/internal/consts"

	"github.com/shirou/gopsutil/v3/process"
)

func WaitAppExit() error {
	// Wait up to 30 seconds for the app to close
	timeout := time.After(30 * time.Second)
	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop()

	for {
		select {
		case <-timeout:
			return fmt.Errorf("timeout waiting for app exit")
		case <-ticker.C:
			procs, err := process.Processes()
			if err != nil {
				return err
			}

			found := false
			for _, p := range procs {
				name, err := p.Name()
				if err == nil && strings.EqualFold(name, consts.AppName) {
					found = true
					break
				}
			}

			if !found {
				return nil
			}
		}
	}
}
