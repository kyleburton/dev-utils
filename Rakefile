require 'rubygems'
require 'fileutils'

$proj_root = File.dirname(__FILE__)

class DevUtilsHelper
  def initialize
    @utilities = [
      {:name => 'make-checkouts',
       :binaries => %w[make-checkouts]},
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
        unless File.exist? dest_file
          FileUtils.ln_s src_file, dest_file
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
