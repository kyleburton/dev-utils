require 'rubygems'
require 'socket'
require 'fcntl'
require 'base_app'

class SwankCli < BaseApp

  def initialize
    super
  end

  def after_init
    host, port = ARGV
    @repl = true
    @rest_line = ''
    @host = host
    @port = port.to_i
    @cmd_counter = 1
    print "connecting to #{@host}:#{@port}\r\n"
    @s = TCPSocket.new @host, @port
    send_command %q|(swank:connection-info)|
    sleep 0.5
    read_any_and_puts
    send_command %q|(swank:create-repl nil)|
    sleep 0.5
    read_any_and_puts
    # $stdin.fcntl(Fcntl::F_SETFL,Fcntl::O_NONBLOCK)
  end

  def command_line_arguments
    printf "here \r\n"
    super.concat [
      ['i','init=s',"Initialize the connetion with code from the given file."],
      ['f','file=s',"Run code in the given file."]
    ]
  end

  def cmd_count
    cmd_num = @cmd_counter
    @cmd_counter +=1
    cmd_num
  end

  def send_command s, ns="user"
    cmd = sprintf %Q|(:emacs-rex %s "%s" t #{cmd_count})|, s, ns
    str = sprintf "%06x%s", cmd.size, cmd
    @verbose and print "Send: #{str}\r\n"
    @s.write str
  end

  def read_any
    @s.read_nonblock 1024*1024
  rescue
    nil
  end

  def read_any_and_puts
    res = read_any
    return if res.nil?
    res.chomp!
    puts "#{res}\r\n"
  end

  def escape_sexp s
    s.gsub! /\\/, "\\\\"
    s.gsub! /"/, "\\\""
    s.gsub! /\r/, "\\r"
    s.gsub! /\n/, "\\n"
    s
  end

  def eval_string s, ns="user"
    # TODO: ensure quoting
    s = sprintf %Q|(swank:interactive-eval "%s")|, escape_sexp(s)
    send_command s, ns
  end

  def read_oneline
    s = @rest_line || ''
    while !s.include?("\n") && !s.include?("\r")
      begin
        read_any_and_puts
        s += $stdin.read_nonblock 1024
      rescue
      end
    end
    s, @rest_line = s.split "\n", 2
    s
  end

  def handle_command cmd
    cmd = cmd[1..-1]
    if !cmd.empty? && "quit".start_with?(cmd)
      @repl = false
    elsif cmd == ""
      printf "Commands: \r\n"
      printf "  ?quit\r\n"
      printf "\r\n"
    else
      printf "Unrecognized command : #{cmd}\r\n"
    end
  end

  def run
    after_init
    if self.init
      eval_string File.read(self.init)
    end

    if self.file
      s = File.read(self.file)
      eval_string s
      await_input = true
      while await_input
        sleep 0.5
        output = read_any
        print "#{output}\r\n"
        await_input = !output.nil?
      end
      return
    end

    while @repl
      read_any_and_puts
      #cmd = $stdin.readline
      cmd = read_oneline
      cmd.chomp!
      if cmd.start_with? "?"
        handle_command cmd
        next
      end
      if cmd.empty?
        read_any_and_puts
        next
      end
      eval_string cmd
    end
  end
end

SwankCli.main
