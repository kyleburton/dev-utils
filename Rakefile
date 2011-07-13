$proj_root = File.dirname(__FILE__)

class DevUtilsHelper
  def initialize
    @utilities = [
      {:name => 'make-checkouts',
       :binaries => %w[bin/make-checkouts]},
    ]
  end

  def install_binaries
    bin_dir = "#{ENV['HOME']}/bin"
    unless File.exist? bin_dir
      FileUtil.mkdir bin_dir
    end

    @utilities.each do |util|
      util[:binaries].each do |bin|
        src_file  = File.join($proj_root,util[:name],bin)
        dest_file = File.join(bin_dir,bin)
        unless File.exist? dest_file
          FileUtil.ln_s src_file, dest_file
        end
      end
    end
  end
end

$utils_helper

desc "Install the utiliites into $HOME/bin (symlink)"
task :install do
  $utiliites.install_binaries
end
