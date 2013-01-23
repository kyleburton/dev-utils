require 'rubygems'
require 'fileutils'

$proj_root = File.dirname(__FILE__)

class DevUtilsHelper
  def initialize
    @utilities = [
      {:name => 'make-checkouts',
       :binaries => %w[make-checkouts]},
      {:name => 'named-screen',
       :binaries => %w[named-screen]},
      {:name => 'cljrep',
       :binaries => %w[cljrep]},
      {:name => 'jenkins-cli',
       :binaries => %w[jenkins]},
      {:name => 'swank-cli',
       :binaries => %w[swank-cli swank-cli.rb]},
      {:name => 'cards',
       :binaries => %w[card]},
      {:name => 'randpass',
       :binaries => %w[randpass]},
      {:name => 'aws',
       :binaries => %w[aws]},
      {:name => 'aws-elb-mon',
       :binaries => %w[aws-elb-mon]},
      {:name => 'remove-from-denyhosts',
       :binaries => %w[remove-from-denyhosts.sh]},
      {:name => 'android-remote-debugger',
       :binaries => %w[android-remote-debugger-daemon]},
    ]
  end

  def install_binaries
    bin_dir = "#{ENV['HOME']}/bin"
    unless File.exist? bin_dir
      FileUtils.mkdir bin_dir
    end

    @utilities.each do |util|
      util[:binaries].each do |bin|
        src_file  = File.join($proj_root,util[:name],'bin',bin)
        dest_file = File.join(bin_dir,bin)
        if File.exist? dest_file
          FileUtils.rm dest_file
        end
        unless File.exist? dest_file
          FileUtils.ln_s src_file, dest_file
        end
      end
      Dir.chdir(util[:name]) do |p|
        if File.exist? "Gemfile"
          puts "running bundler for #{util[:name]}..."
          system "bundle install"
        end
      end
    end
  end
end

$utils_helper = DevUtilsHelper.new

desc "Install the utiliites into $HOME/bin (symlink)"
task :install do
  $utils_helper.install_binaries
end
