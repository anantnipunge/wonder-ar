package com.example.fragments.ar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.aroom.R
import com.example.fragments.shopping.ProductDetailsFragmentArgs
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VrFragment : Fragment(R.layout.fragment_vr) {

    private val args by navArgs<ProductDetailsFragmentArgs>()

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference.child("models")

    private val modelsToLoad = listOf(
        "1706099350306_chairmodel.glb",
        "lilly.glb",
        // Add more model file names here as needed
    )

    private var currentModelIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product
        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
            }
            setOnTapArPlaneListener(::onTapPlane)
        }
    }

    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (currentModelIndex < modelsToLoad.size) {
            val modelFileName = modelsToLoad[currentModelIndex]
            val modelUri = storageRef.child(modelFileName).downloadUrl

            lifecycleScope.launch {
                try {
                    val url = modelUri.await().toString()
                    val model = loadModel(url);
                    addToScene(hitResult, model);
                    currentModelIndex++;
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading model: ${e.message}")
                }
            }
        } else {
            Toast.makeText(context, "All models loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadModel(modelUri: String): Renderable {
        return ModelRenderable.builder()
            .setSource(context, Uri.parse(modelUri))
            .setIsFilamentGltf(true)
            .build()
            .await()
    }

    private fun addToScene(hitResult: HitResult, model: Renderable) {
        // Create the Anchor.
        scene.addChild(AnchorNode(hitResult.createAnchor()).apply {
            // Create the transformable model and add it to the anchor.
            addChild(TransformableNode(arFragment.transformationSystem).apply {
                renderable = model
                localPosition = Vector3(0.0f, 0.0f, 0.0f)
                localScale = Vector3(0.7f, 0.7f, 0.7f)
                renderableInstance.setCulling(false)
                renderableInstance.animate(true).start()
            })
        })
    }

    companion object {
        private const val TAG = "ArFragment"
    }
}


/**
 *
 * <---------------FOR SINGLE MODEL RENDERING-------------->
 *
 * package com.example.fragments.ar
 *
 * import android.net.Uri
 * import android.os.Bundle
 * import android.view.MotionEvent
 * import android.view.View
 * import android.widget.Toast
 * import androidx.fragment.app.Fragment
 * import androidx.lifecycle.lifecycleScope
 * import androidx.navigation.fragment.navArgs
 * import com.example.aroom.R
 * import com.example.fragments.shopping.ProductDetailsFragmentArgs
 * import com.google.ar.core.HitResult
 * import com.google.ar.core.Plane
 * import com.google.ar.sceneform.AnchorNode
 * import com.google.ar.sceneform.Node
 * import com.google.ar.sceneform.SceneView
 * import com.google.ar.sceneform.math.Vector3
 * import com.google.ar.sceneform.rendering.ModelRenderable
 * import com.google.ar.sceneform.rendering.Renderable
 * import com.google.ar.sceneform.rendering.ViewRenderable
 * import com.google.ar.sceneform.ux.ArFragment
 * import com.google.ar.sceneform.ux.TransformableNode
 * import com.google.firebase.storage.FirebaseStorage
 * import com.google.firebase.storage.StorageReference
 * import com.gorisse.thomas.sceneform.scene.await
 * import kotlinx.coroutines.tasks.await
 *
 * class VrFragment : Fragment(R.layout.fragment_vr) {
 *
 *     private val args by navArgs<ProductDetailsFragmentArgs>()
 *
 *     private lateinit var arFragment: ArFragment
 *     private val arSceneView get() = arFragment.arSceneView
 *     private val scene get() = arSceneView.scene
 *     private var model: Renderable? = null
 *     private var modelView: ViewRenderable? = null
 *     private val storage = FirebaseStorage.getInstance()
 *     private val storageRef: StorageReference = storage.reference.child("models")
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         val product = args.product
 * //        val model = product.model
 *         arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
 *             setOnSessionConfigurationListener { session, config ->
 *                 // Modify the AR session configuration here
 *             }
 *             setOnViewCreatedListener { arSceneView ->
 *                 arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
 *             }
 *             setOnTapArPlaneListener(::onTapPlane)
 *         }
 *
 *         lifecycleScope.launchWhenCreated {
 *             val model = storageRef.child("coffee_table.glb").downloadUrl.await().toString()
 *             loadModels(model)
 *         }
 *     }
 *
 *
 *     private suspend fun loadModels(modelUri : String?) {
 *         model = ModelRenderable.builder()
 *             .setSource(context, Uri.parse(modelUri))
 *             .setIsFilamentGltf(true)
 *             .await()
 *         modelView = ViewRenderable.builder()
 *            .setView(context, R.layout.viewpager_image_item)
 *             .await()
 *     }
 *
 *     private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
 *         if (model == null || modelView == null) {
 *             Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
 *             return
 *         }
 *
 *         // Create the Anchor.
 *         scene.addChild(AnchorNode(hitResult.createAnchor()).apply {
 *             // Create the transformable model and add it to the anchor.
 *             addChild(TransformableNode(arFragment.transformationSystem).apply {
 *                 renderable = model
 *                 renderableInstance.setCulling(false)
 *                 renderableInstance.animate(true).start()
 *                 // Add the View
 *                 addChild(Node().apply {
 *                     // Define the relative position
 *                     localPosition = Vector3(0.0f, 1f, 0.0f)
 *                     localScale = Vector3(0.7f, 0.7f, 0.7f)
 *                     renderable = modelView
 *                 })
 *             })
 *         })
 *     }
 *
 * }
 */


