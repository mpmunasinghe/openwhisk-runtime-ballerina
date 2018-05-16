package main

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"os/exec"
	"encoding/base64"
)


func InitHandler(writer http.ResponseWriter, request *http.Request) {
	// Decode request message body
	decoder := json.NewDecoder(request.Body)

	var init Init
	var fileName string
	var content []byte

	err := decoder.Decode(&init)
	if err != nil {
		panic(err)
	}
	defer request.Body.Close()

	// Write function to a file
	if init.Value.Binary {
		fileName = "function.balx"
		sDec, _ := base64.StdEncoding.DecodeString(init.Value.Code)
		content = []byte(sDec)
	} else {
		fileName = "function.bal"
		content = []byte(init.Value.Code)
	}

	err = ioutil.WriteFile(fileName, content, 0644)
	if err != nil {
		panic(err)
	}

	// Compile ballerina function if the file is not a binary
	if !init.Value.Binary {
		_, err = exec.Command("sh", "-c", "ballerina build function.bal").Output()
		if err != nil {
			panic(err)
		}
	}

	writer.WriteHeader(http.StatusOK)
}

func RunHandler(writer http.ResponseWriter, request *http.Request) {
	// Execute ballerina function
	out, err := exec.Command("sh", "-c", "ballerina run function.balx").Output()
	if err != nil {
		panic(err)
	}
	log.Printf("%s", out)
	writer.Write(out)
}
