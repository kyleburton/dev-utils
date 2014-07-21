require 'rubygems'
require 'fileutils'

$proj_root = File.dirname(__FILE__)

class DevUtilsHelper
  def initialize
    @utilities = [
      {:name => 'bash-utils',
       :binaries => %w[isodate]},
      {:name => 'make-checkouts',
       :binaries => %w[make-checkouts]},
      {:name => 'open-port',
       :binaries => %w[open-port]},
      {:name => 'swank',
       :binaries => %w[swank]},
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
      {:name => 'browserstack-tunnel',
       :binaries => %w[browserstack-tunnel]},
      {:name => 'auto-jslint',
       :binaries => %w[auto-jslint]},
      {:name => 'bash-utils',
       :binaries => %w[rn_pair_name]},
      {:name => 'emacs-cli',
       :binaries => Dir["emacs-cli/bin/*"].map {|f| File.basename(f)}},
      {:name => 'auto-swank',
       :binaries => Dir["auto-swank/bin/*"].map {|f| File.basename(f)}},
      {:name => 'instago', :install => ['rake install']}
    ]
  end

  def install_binaries
    bin_dir = "#{ENV['HOME']}/bin"
    unless File.exist? bin_dir
      FileUtils.mkdir bin_dir
    end

    @utilities.each do |util|
      (util[:binaries]||[]).each do |bin|
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

        (util[:install]||[]).each do |cmd|
          system cmd
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
