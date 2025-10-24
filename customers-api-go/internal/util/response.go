package util

import (
	"github.com/gin-gonic/gin"
)

func JSON(c *gin.Context, status int, body interface{}) {
	c.JSON(status, body)
}

func ErrorJSON(c *gin.Context, status int, message string) {
	c.JSON(status, gin.H{"error": message})
}
