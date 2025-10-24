package util

import "github.com/gin-gonic/gin"

func JSONOK(c *gin.Context, v interface{}) {
	c.JSON(200, v)
}

func JSONError(c *gin.Context, code int, msg string) {
	c.JSON(code, gin.H{"error": msg})
}
