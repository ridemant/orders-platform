package logger

import (
	"strings"

	"github.com/sirupsen/logrus"
)

func Init(level string) {
	lvl, err := logrus.ParseLevel(strings.ToLower(level))
	if err != nil {
		lvl = logrus.InfoLevel
	}
	logrus.SetLevel(lvl)
	logrus.SetFormatter(&logrus.TextFormatter{
		FullTimestamp: true,
	})
}
