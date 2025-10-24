package logger

import (
	"log"
	"os"
)

// Logger is a minimal wrapper over the standard logger providing a few helpers.
type Logger struct {
	l *log.Logger
}

// New creates a new Logger. level is accepted but not used in this minimal impl.
func New(level string) *Logger {
	return &Logger{l: log.New(os.Stdout, "", log.LstdFlags|log.Lmsgprefix)}
}

func (lg *Logger) Info(msg string) {
	lg.l.SetPrefix("INFO: ")
	lg.l.Println(msg)
}

func (lg *Logger) Infof(format string, v ...interface{}) {
	lg.l.SetPrefix("INFO: ")
	lg.l.Printf(format, v...)
}

func (lg *Logger) Errorf(format string, v ...interface{}) {
	lg.l.SetPrefix("ERROR: ")
	lg.l.Printf(format, v...)
}

func (lg *Logger) Fatalf(format string, v ...interface{}) {
	lg.l.SetPrefix("FATAL: ")
	lg.l.Fatalf(format, v...)
}
