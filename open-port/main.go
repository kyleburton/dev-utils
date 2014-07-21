package main

import (
  "flag"
  "fmt"
  "net"
  "os"
)

var Verbose = false

func TryPort (addr string) bool {
  if (Verbose) {
    fmt.Fprintf(os.Stderr, "Trying: %s...", addr)
  }
  l, err := net.Listen("tcp", addr)
  if err != nil {
    if (Verbose) {
      fmt.Fprintf(os.Stderr, "in use\n")
    }
    return false
  }

  if (Verbose) {
    fmt.Fprintf(os.Stderr, "available\n")
  }

  l.Close()
  return true
}



func main () {
  var startPort, endPort int
  flag.IntVar(&startPort, "start-port", 4005, "Starting port.")
  flag.IntVar(&endPort,   "end-port",   4999, "Ending Port")
  flag.BoolVar(&Verbose,  "verbose",    false, "Be verbose")
  flag.Parse()

  if (Verbose) {
    fmt.Fprintf(os.Stderr, "Finding port from: %d to %d\n", startPort, endPort)
  }

  for ii := startPort; ii <= endPort; ii += 1 {
    if (TryPort(fmt.Sprintf("127.0.0.1:%d", ii))) {
      fmt.Fprintf(os.Stdout, "%d\n", ii)
      break;
    }
  }
}
