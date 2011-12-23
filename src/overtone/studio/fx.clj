(ns
    ^{:doc "Audio effects library"
      :author "Jeff Rose"}
  overtone.studio.fx
  (:use [overtone.libs event]
        [overtone.sc synth gens]))

(defsynth fx-noise-gate
  "A noise gate only lets audio above a certain amplitude threshold through.  Often used to filter out hardware circuit noise or unwanted background noise."
  [bus 0 threshold 0.4
   slope-below 1 slope-above 0.1
   clamp-time 0.01 relax-time 0.1]
  (let [source (in bus)
        gated (compander source source threshold
                    slope-below slope-above
                    clamp-time relax-time)]
    (replace-out bus gated)))

(defsynth fx-compressor
  "A compressor clamps audio signals above an amplitude threshold down, compressing the dynamic range.  Used to normalize a poppy sound so that the amplitude is more consistent, or as a sound warping effect.  The clamp time determines the delay from when the signal is detected as going over the threshold to when clamping begins, and the slope determines the rate at which the clamp occurs."
  [bus 0 threshold 0.2
   slope-below 1 slope-above 0.5
   clamp-time 0.01 relax-time 0.01]
  (let [source (in bus)]
    (replace-out bus
         (compander source source threshold
                    slope-below slope-above
                    clamp-time relax-time))))

(defsynth fx-limiter
  "A limiter sets a maximum threshold for the audio amplitude, and anything above this threshold is quickly clamped down to within it."
  [bus 0 threshold 0.2
   slope-below 1 slope-above 0.1
   clamp-time 0.01 relax-time 0.01]
  (let [source (in bus)]
    (replace-out bus
         (compander source source threshold
                    slope-below slope-above
                    clamp-time relax-time))))

(defsynth fx-sustainer
  [bus 0 threshold 0.2
   slope-below 1 slope-above 0.5
   clamp-time 0.01 relax-time 0.01]
  (let [source (in bus)]
    (replace-out bus
         (compander source source threshold
                    slope-below slope-above
                    clamp-time relax-time))))

(defsynth fx-reverb1
  "Uses the free-verb ugen."
  [bus 0 wet-dry 0.5 room-size 0.5 dampening 0.5]
  (let [source (in bus)
        verbed (free-verb source wet-dry room-size dampening)]
    (replace-out bus (* 1.4 verbed))))

(defsynth fx-reverb2
  "Implements Schroeder reverb using delays."
  [bus 0]
  (let [input (in bus)
        delrd (local-in 4)
        output (+ input [(first delrd) (second delrd)])
        sig [(+ (first output) (second output)) (- (first output) (second output))
             (+ (nth delrd 2) (nth delrd 3)) (- (nth delrd 2) (nth delrd 3))]
        sig [(+ (nth sig 0) (nth sig 2)) (+ (nth sig 1) (nth sig 3))
             (- (nth sig 0) (nth sig 2)) (- (nth sig 0) (nth sig 2))]
        sig (* sig [0.4 0.37 0.333 0.3])
        deltimes (- (* [101 143 165 177] 0.001) (control-dur))
        lout (local-out (delay-c sig deltimes deltimes))]
    (replace-out bus output)))

(defsynth fx-echo
  [bus 0 max-delay 1.0 delay-time 0.4 decay-time 2.0]
  (let [source (in bus)
        echo (comb-n source max-delay delay-time decay-time)]
    (replace-out bus (pan2 (+ echo source) 0))))

(defsynth fx-chorus
  [bus 0 rate 0.002 depth 0.01]
  (let [src (in bus)
        dub-depth (* 2 depth)
        rates [rate (+ rate 0.001)]
        osc (+ dub-depth (* dub-depth (sin-osc:kr rates)))
        dly-a (delay-l src 0.3 osc)
        sig (apply + src dly-a)]
    (replace-out bus (* 0.3 sig))))

(defsynth fx-distortion
  [bus 0 boost 4 level 0.01]
  (let [src (in bus)]
    (replace-out bus (distort (* boost (clip2 src level))))))

(defsynth fx-rlpf
  [bus 0 cutoff 20000 res 0.6]
  (let [src (in bus)]
    (replace-out bus (rlpf src cutoff res))))

(defsynth fx-rhpf
  [bus 0 cutoff 2 res 0.6]
  (let [src (in bus)]
    (replace-out bus (rhpf src cutoff res))))

(defsynth eq-3
  [bus 0 pan 0 volume 1
   low-freq 80 mid-freq 800 hi-freq 2000 mix 1
   low-gain -45 mid-gain -45 hi-gain -45]
  (let [dry (in bus)
        wet (b-low-shelf dry low-freq 1 low-gain)
        wet (b-peak-eq wet mid-freq 1 mid-gain)
        wet (b-hi-shelf wet hi-freq 1 hi-gain)
        mixed (x-fade2 dry wet mix)]
    (replace-out bus (pan2 mixed pan volume))))

